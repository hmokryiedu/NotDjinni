---
name: merge-worktree-context
description: Sync project context updates from sibling git worktrees into the current branch. Use when Codex needs to scan ../not-djinni-worktrees for updated AGENTS.md, ai/rules/**, or .codex/** files, show a dry-run summary, ask for approval, and apply approved context-file changes without deleting target-only files.
---

# Merge Worktree Context

Synchronize reusable agent/project context from sibling worktrees into the current repository branch.

## Scope

Only these paths are in scope:

- `AGENTS.md`
- `ai/rules/**`
- `.codex/**`

Never delete target files that are missing from source worktrees. Only add or update files in scope.

## Workflow

1. Run from the target repository root.
2. Dry-run first:
   ```bash
   python3 .codex/skills/merge-worktree-context/scripts/sync_worktree_context.py
   ```
3. Read the summary:
   - `updates` are safe to apply automatically after user approval.
   - `conflicts` mean multiple worktrees have different versions for the same path.
   - `unchanged` means a source version matches the current target.
4. Show the user the visible summary before any write.
5. Ask for approval with `request_user_input`.
6. If conflicts exist, ask which worktree version to use for each conflict. If no choice is made, skip that conflict.
7. Apply approved updates:
   ```bash
   python3 .codex/skills/merge-worktree-context/scripts/sync_worktree_context.py --apply
   ```
8. For conflict choices, write a temporary JSON file and pass it with `--choices`:
   ```json
   {
     "AGENTS.md": "feature-a",
     ".codex/skills/example/SKILL.md": "feature-b"
   }
   ```
   ```bash
   python3 .codex/skills/merge-worktree-context/scripts/sync_worktree_context.py --apply --choices /path/to/choices.json
   ```

## Source Discovery

The script scans direct children of `../not-djinni-worktrees/`.

A source is accepted only when it looks like a git worktree root:

- `.git` file exists, or
- `.git` directory exists.

The current working directory is always the target repository/branch.

## Conflict Rules

- One changed source version for a path -> auto-applicable update.
- Multiple changed source versions for the same path -> conflict.
- Conflict without explicit choice -> skipped.
- Choice value may be a worktree directory name or an absolute source path.

## Output

Use `--json` when another script needs structured output:

```bash
python3 .codex/skills/merge-worktree-context/scripts/sync_worktree_context.py --json
```

The JSON includes:

- `target`
- `worktrees_dir`
- `sources`
- `updates`
- `conflicts`
- `unchanged_count`
- `applied`
- `skipped_conflicts`
