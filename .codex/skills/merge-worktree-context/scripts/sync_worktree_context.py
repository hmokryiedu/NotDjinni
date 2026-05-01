#!/usr/bin/env python3
"""Sync AGENTS.md, ai/rules/**, and .codex/** from sibling worktrees."""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any


DEFAULT_WORKTREES_DIR = "../not-djinni-worktrees"
SYNC_ROOTS = ("AGENTS.md", "ai/rules", ".codex")


@dataclass(frozen=True)
class Candidate:
    relpath: str
    source_root: Path
    source_name: str
    source_file: Path
    sha256: str
    size: int


def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def is_repo_root(path: Path) -> bool:
    return (path / ".git").exists()


def iter_scope_files(root: Path) -> dict[str, Path]:
    files: dict[str, Path] = {}

    agents = root / "AGENTS.md"
    if agents.is_file():
        files["AGENTS.md"] = agents

    for scoped_root in (root / "ai" / "rules", root / ".codex"):
        if not scoped_root.is_dir():
            continue
        for file_path in scoped_root.rglob("*"):
            if file_path.is_file():
                relpath = file_path.relative_to(root).as_posix()
                files[relpath] = file_path

    return files


def discover_sources(target: Path, worktrees_dir: Path) -> list[Path]:
    if not worktrees_dir.is_dir():
        return []

    sources: list[Path] = []
    target_resolved = target.resolve()
    for child in sorted(worktrees_dir.iterdir(), key=lambda item: item.name):
        if not child.is_dir() or not is_repo_root(child):
            continue
        if child.resolve() == target_resolved:
            continue
        sources.append(child)
    return sources


def build_plan(target: Path, worktrees_dir: Path) -> dict[str, Any]:
    target_files = iter_scope_files(target)
    target_hashes = {
        relpath: file_sha256(path)
        for relpath, path in target_files.items()
    }

    candidates_by_path: dict[str, list[Candidate]] = {}
    sources = discover_sources(target, worktrees_dir)
    for source_root in sources:
        for relpath, source_file in iter_scope_files(source_root).items():
            candidate = Candidate(
                relpath=relpath,
                source_root=source_root,
                source_name=source_root.name,
                source_file=source_file,
                sha256=file_sha256(source_file),
                size=source_file.stat().st_size,
            )
            candidates_by_path.setdefault(relpath, []).append(candidate)

    updates: list[dict[str, Any]] = []
    conflicts: list[dict[str, Any]] = []
    unchanged_count = 0

    for relpath in sorted(candidates_by_path):
        target_hash = target_hashes.get(relpath)
        changed = [
            candidate
            for candidate in candidates_by_path[relpath]
            if candidate.sha256 != target_hash
        ]

        if not changed:
            unchanged_count += 1
            continue

        unique_by_hash: dict[str, list[Candidate]] = {}
        for candidate in changed:
            unique_by_hash.setdefault(candidate.sha256, []).append(candidate)

        if len(unique_by_hash) == 1:
            selected = sorted(changed, key=lambda item: item.source_name)[0]
            updates.append(candidate_to_update(selected, "update" if relpath in target_files else "add"))
            continue

        conflicts.append({
            "path": relpath,
            "type": "update" if relpath in target_files else "add",
            "candidates": [
                candidate_to_update(group[0], "update" if relpath in target_files else "add")
                for _, group in sorted(unique_by_hash.items())
            ],
        })

    return {
        "target": str(target),
        "worktrees_dir": str(worktrees_dir),
        "sources": [str(source) for source in sources],
        "updates": updates,
        "conflicts": conflicts,
        "unchanged_count": unchanged_count,
        "applied": [],
        "skipped_conflicts": [],
    }


def candidate_to_update(candidate: Candidate, change_type: str) -> dict[str, Any]:
    return {
        "path": candidate.relpath,
        "type": change_type,
        "source_worktree": candidate.source_name,
        "source_path": str(candidate.source_file),
        "sha256": candidate.sha256,
        "size": candidate.size,
        "area": area_for_path(candidate.relpath),
    }


def area_for_path(relpath: str) -> str:
    if relpath == "AGENTS.md":
        return "AGENTS"
    if relpath.startswith("ai/rules/"):
        return "ai/rules"
    if relpath.startswith(".codex/"):
        return ".codex"
    return "unknown"


def read_choices(path: Path | None) -> dict[str, str]:
    if path is None:
        return {}
    with path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)
    if not isinstance(data, dict):
        raise ValueError("--choices must point to a JSON object")
    return {str(key): str(value) for key, value in data.items()}


def apply_file(target: Path, item: dict[str, Any]) -> None:
    destination = target / item["path"]
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(item["source_path"], destination)


def apply_plan(plan: dict[str, Any], target: Path, choices: dict[str, str]) -> None:
    applied: list[dict[str, Any]] = []
    skipped_conflicts: list[dict[str, Any]] = []

    for update in plan["updates"]:
        apply_file(target, update)
        applied.append(update)

    for conflict in plan["conflicts"]:
        choice = choices.get(conflict["path"])
        selected = select_conflict_candidate(conflict, choice)
        if selected is None:
            skipped_conflicts.append({
                "path": conflict["path"],
                "reason": "no matching choice" if choice else "no choice",
                "choice": choice,
            })
            continue
        apply_file(target, selected)
        applied.append(selected)

    plan["applied"] = applied
    plan["skipped_conflicts"] = skipped_conflicts


def select_conflict_candidate(conflict: dict[str, Any], choice: str | None) -> dict[str, Any] | None:
    if not choice:
        return None
    for candidate in conflict["candidates"]:
        if choice == candidate["source_worktree"] or choice == candidate["source_path"]:
            return candidate
    return None


def print_readable(plan: dict[str, Any]) -> None:
    print("Worktree context sync")
    print(f"Target: {plan['target']}")
    print(f"Worktrees dir: {plan['worktrees_dir']}")
    print(f"Sources: {len(plan['sources'])}")

    if not plan["sources"]:
        print("No source worktrees found.")

    if not plan["updates"] and not plan["conflicts"]:
        print("No context updates.")
    else:
        print(f"Updates: {len(plan['updates'])}")
        for item in plan["updates"]:
            print(f"  [{item['type']}] {item['path']} <- {item['source_worktree']} ({item['area']})")

        print(f"Conflicts: {len(plan['conflicts'])}")
        for conflict in plan["conflicts"]:
            print(f"  [conflict] {conflict['path']}")
            for candidate in conflict["candidates"]:
                short_hash = candidate["sha256"][:12]
                print(f"    - {candidate['source_worktree']} sha={short_hash} size={candidate['size']}")

    print(f"Unchanged paths: {plan['unchanged_count']}")

    if plan["applied"] or plan["skipped_conflicts"]:
        print(f"Applied: {len(plan['applied'])}")
        for item in plan["applied"]:
            print(f"  [{item['type']}] {item['path']} <- {item['source_worktree']}")
        print(f"Skipped conflicts: {len(plan['skipped_conflicts'])}")
        for item in plan["skipped_conflicts"]:
            print(f"  [skip] {item['path']} ({item['reason']})")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Sync AGENTS.md, ai/rules/**, and .codex/** from ../not-djinni-worktrees.",
    )
    parser.add_argument(
        "--worktrees-dir",
        default=DEFAULT_WORKTREES_DIR,
        help="Directory containing sibling worktrees. Default: ../not-djinni-worktrees",
    )
    parser.add_argument("--apply", action="store_true", help="Copy approved updates into the current repo.")
    parser.add_argument("--choices", type=Path, help="JSON object mapping conflict path to worktree name or source path.")
    parser.add_argument("--json", action="store_true", help="Print structured JSON output.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    target = Path.cwd()
    if not is_repo_root(target):
        print("Error: run from a git repository root.", file=sys.stderr)
        return 2

    worktrees_dir = (target / args.worktrees_dir).resolve()
    try:
        choices = read_choices(args.choices)
        plan = build_plan(target, worktrees_dir)
        if args.apply:
            apply_plan(plan, target, choices)
    except Exception as error:
        print(f"Error: {error}", file=sys.stderr)
        return 1

    if args.json:
        print(json.dumps(plan, indent=2, sort_keys=True))
    else:
        print_readable(plan)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
