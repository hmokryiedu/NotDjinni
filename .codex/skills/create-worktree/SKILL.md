---
name: create-worktree
description: Create a new git worktree for parallel development, copy local project context files and folders, and initialize submodules. Use when asked to create a worktree, set up a parallel branch workspace, or prepare a ready-to-work copy of this repository.
---

# Create Worktree

Create a new worktree and prepare it for development with all required local context.

## Required Parameters

Ask the user for:
- **Branch name**: Full branch name (for example `feature/my-feature`)
- **Worktree name**: Folder name for the worktree (created under `../not-djinni-worktrees/`)

## Workflow

1. Create parent directory:
   ```bash
   mkdir -p ../not-djinni-worktrees
   ```

2. Clean stale worktree metadata before creating a new worktree:
   ```bash
   git worktree prune
   ```

3. Create the worktree:
   ```bash
   git worktree add ../not-djinni-worktrees/<worktree_name> -b <branch_name>
   ```
   If `<branch_name>` already exists because of a previously interrupted run, reuse it instead:
   ```bash
   git worktree add ../not-djinni-worktrees/<worktree_name> <branch_name>
   ```

4. Copy local properties:
   ```bash
   cp local.properties ../not-djinni-worktrees/<worktree_name>/
   ```

5. Copy local context directories (overwrite if files already exist):
   ```bash
   for dir in .claude .codex ai; do
     if [ -d "$dir" ]; then
       cp -R "$dir" "../not-djinni-worktrees/<worktree_name>/"
     fi
   done
   for file in AGENTS.md .ignore; do
     if [ -e "$file" ]; then
       cp "$file" "../not-djinni-worktrees/<worktree_name>/"
     fi
   done
   ```

6. Initialize submodules in the new worktree:
   ```bash
   cd ../not-djinni-worktrees/<worktree_name> && git submodule update --init --recursive
   ```

## Notes

- Run all commands from the main repository root.
- Worktree location is `../not-djinni-worktrees/<worktree_name>`.
- Shared worktrees live under `../not-djinni-worktrees`.
- The branch is created by `git worktree add ... -b ...` only when it does not already exist.
- If a previous run was interrupted, always run `git worktree prune` first and then reuse the existing branch when the target path is free.
- Skip copying any directory from the list if it does not exist in the source repository.
- Skip copying any file from the list if it does not exist in the source repository.
