#!/usr/bin/env python3
"""
Thematic API Migration Script

This script helps migrate your Minecraft mod from the old package structure
(com.funalex.themAnim) to the new thematic-api package structure (bond.thematic.api).

Usage:
    python migrate_to_thematic_api.py /path/to/your/mod/src

Features:
- Automatically updates all Java import statements
- Updates package declarations in Java files
- Updates build.gradle dependency declarations
- Updates mixin configuration files
- Creates backup of original files before modification
- Provides detailed migration report

Requirements:
- Python 3.6+
- Your mod source code
"""

import os
import re
import sys
import shutil
import argparse
from pathlib import Path
from typing import Dict, List, Set, Tuple

class ThematicAPIMigrator:
    def __init__(self, dry_run: bool = False, verbose: bool = False):
        self.dry_run = dry_run
        self.verbose = verbose
        self.changes_made = 0
        self.files_processed = 0
        self.backup_dir = None
        
        # Mapping of old packages to new packages
        self.package_mappings = {
            'com.funalex.themAnim': 'bond.thematic.api',
            'com.github.awitkowski0:minecraftPlayerAnimatorSynced': 'bond.thematic:thematic-api',
            'playeranimator': 'thematic-api',
            'themAnimator': 'thematic-api',
            'thematic-animation-lib': 'thematic-api'
        }
        
        # File patterns to process
        self.java_pattern = re.compile(r'\.java$')
        self.gradle_pattern = re.compile(r'build\.gradle$')
        self.json_pattern = re.compile(r'\.json$')
        self.toml_pattern = re.compile(r'\.toml$')
    
    def create_backup(self, source_path: Path) -> Path:
        """Create a backup of the source directory before making changes."""
        backup_name = f"{source_path.name}_backup_before_thematic_migration"
        backup_path = source_path.parent / backup_name
        
        if backup_path.exists():
            # Add timestamp if backup already exists
            import datetime
            timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            backup_path = source_path.parent / f"{backup_name}_{timestamp}"
        
        if not self.dry_run:
            shutil.copytree(source_path, backup_path)
            print(f"✅ Created backup at: {backup_path}")
        else:
            print(f"[DRY RUN] Would create backup at: {backup_path}")
        
        self.backup_dir = backup_path
        return backup_path
    
    def update_java_file(self, file_path: Path) -> int:
        """Update Java file with new package structure."""
        changes = 0
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Update package declarations
            content = re.sub(
                r'package\s+com\.funalex\.themAnim(\.[a-zA-Z0-9_.]+)?;',
                r'package bond.thematic.api\1;',
                content
            )
            
            # Update import statements
            content = re.sub(
                r'import\s+com\.funalex\.themAnim(\.[a-zA-Z0-9_.]+)?(\.[A-Za-z0-9_]+)?;',
                r'import bond.thematic.api\1\2;',
                content
            )
            
            # Update static imports
            content = re.sub(
                r'import\s+static\s+com\.funalex\.themAnim(\.[a-zA-Z0-9_.]+)?(\.[A-Za-z0-9_]+)?;',
                r'import static bond.thematic.api\1\2;',
                content
            )
            
            if content != original_content:
                changes += content.count('bond.thematic.api') - original_content.count('bond.thematic.api')
                
                if not self.dry_run:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                
                if self.verbose:
                    relative_path = file_path.relative_to(Path.cwd()) if file_path.is_relative_to(Path.cwd()) else file_path
                    print(f"  📝 Updated Java file: {relative_path} ({changes} changes)")
        
        except Exception as e:
            print(f"❌ Error processing Java file {file_path}: {e}")
        
        return changes
    
    def update_gradle_file(self, file_path: Path) -> int:
        """Update Gradle build files with new dependency declarations."""
        changes = 0
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Update Maven coordinates
            content = re.sub(
                r'[\'"]com\.github\.awitkowski0:minecraftPlayerAnimatorSynced:[^\'\"]+[\'"]',
                r'"bond.thematic:thematic-api:${project.thematic_api_version}"',
                content
            )
            
            # Update repository references
            content = re.sub(
                r'minecraftPlayerAnimatorSynced',
                r'thematic-api',
                content
            )
            
            # Update old mod ID references
            content = re.sub(
                r'[\'"]playeranimator[\'"]',
                r'"thematic-api"',
                content
            )
            
            content = re.sub(
                r'[\'"]themAnimator[\'"]',
                r'"thematic-api"',
                content
            )
            
            if content != original_content:
                changes += 1
                
                if not self.dry_run:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                
                if self.verbose:
                    relative_path = file_path.relative_to(Path.cwd()) if file_path.is_relative_to(Path.cwd()) else file_path
                    print(f"  🔧 Updated Gradle file: {relative_path}")
        
        except Exception as e:
            print(f"❌ Error processing Gradle file {file_path}: {e}")
        
        return changes
    
    def update_json_file(self, file_path: Path) -> int:
        """Update JSON configuration files (mixins.json, fabric.mod.json, etc.)."""
        changes = 0
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Update package references in JSON files
            content = re.sub(
                r'"com\.funalex\.themAnim([^"]*)"',
                r'"bond.thematic.api\1"',
                content
            )
            
            # Update mod ID references
            content = re.sub(
                r'"playeranimator"',
                r'"thematic-api"',
                content
            )
            
            content = re.sub(
                r'"themAnimator"',
                r'"thematic-api"',
                content
            )
            
            # Update mixin configuration references
            content = re.sub(
                r'"themAnimator-common\.mixins\.json"',
                r'"thematic-api-common.mixins.json"',
                content
            )
            
            if content != original_content:
                changes += 1
                
                if not self.dry_run:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                
                if self.verbose:
                    relative_path = file_path.relative_to(Path.cwd()) if file_path.is_relative_to(Path.cwd()) else file_path
                    print(f"  📄 Updated JSON file: {relative_path}")
        
        except Exception as e:
            print(f"❌ Error processing JSON file {file_path}: {e}")
        
        return changes
    
    def update_toml_file(self, file_path: Path) -> int:
        """Update TOML files (mods.toml for Forge)."""
        changes = 0
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            
            # Update mod ID in TOML
            content = re.sub(
                r'modId\s*=\s*[\'"]playeranimator[\'"]',
                r'modId = "thematic-api"',
                content
            )
            
            content = re.sub(
                r'modId\s*=\s*[\'"]themAnimator[\'"]',
                r'modId = "thematic-api"',
                content
            )
            
            if content != original_content:
                changes += 1
                
                if not self.dry_run:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                
                if self.verbose:
                    relative_path = file_path.relative_to(Path.cwd()) if file_path.is_relative_to(Path.cwd()) else file_path
                    print(f"  📋 Updated TOML file: {relative_path}")
        
        except Exception as e:
            print(f"❌ Error processing TOML file {file_path}: {e}")
        
        return changes
    
    def scan_directory(self, root_path: Path) -> List[Path]:
        """Scan directory for files that need migration."""
        files_to_process = []
        
        for file_path in root_path.rglob('*'):
            if not file_path.is_file():
                continue
                
            if (self.java_pattern.search(str(file_path)) or 
                self.gradle_pattern.search(str(file_path)) or
                self.json_pattern.search(str(file_path)) or
                self.toml_pattern.search(str(file_path))):
                files_to_process.append(file_path)
        
        return files_to_process
    
    def migrate(self, source_path: Path) -> Dict[str, int]:
        """Perform the migration on the given source path."""
        print(f"🚀 Starting Thematic API migration for: {source_path}")
        
        if not source_path.exists():
            raise FileNotFoundError(f"Source path does not exist: {source_path}")
        
        # Create backup unless it's a dry run
        if not self.dry_run:
            self.create_backup(source_path)
        else:
            print("[DRY RUN] Skipping backup creation")
        
        # Scan for files to process
        files_to_process = self.scan_directory(source_path)
        
        print(f"📊 Found {len(files_to_process)} files to process")
        
        stats = {
            'java_files': 0,
            'gradle_files': 0,
            'json_files': 0,
            'toml_files': 0,
            'total_changes': 0
        }
        
        # Process each file type
        for file_path in files_to_process:
            self.files_processed += 1
            
            if self.java_pattern.search(str(file_path)):
                changes = self.update_java_file(file_path)
                stats['java_files'] += 1
                stats['total_changes'] += changes
                
            elif self.gradle_pattern.search(str(file_path)):
                changes = self.update_gradle_file(file_path)
                stats['gradle_files'] += 1
                stats['total_changes'] += changes
                
            elif self.json_pattern.search(str(file_path)):
                changes = self.update_json_file(file_path)
                stats['json_files'] += 1
                stats['total_changes'] += changes
                
            elif self.toml_pattern.search(str(file_path)):
                changes = self.update_toml_file(file_path)
                stats['toml_files'] += 1
                stats['total_changes'] += changes
        
        self.changes_made = stats['total_changes']
        return stats
    
    def print_migration_report(self, stats: Dict[str, int], source_path: Path):
        """Print a detailed migration report."""
        print(f"\n📋 Migration Report for {source_path}")
        print("=" * 50)
        print(f"Files processed: {self.files_processed}")
        print(f"  • Java files: {stats['java_files']}")
        print(f"  • Gradle files: {stats['gradle_files']}")
        print(f"  • JSON files: {stats['json_files']}")
        print(f"  • TOML files: {stats['toml_files']}")
        print(f"Total changes made: {stats['total_changes']}")
        
        if self.backup_dir and not self.dry_run:
            print(f"Backup created at: {self.backup_dir}")
        
        print("\n📝 What was changed:")
        print("  • Package declarations: com.funalex.themAnim → bond.thematic.api")
        print("  • Import statements: com.funalex.themAnim → bond.thematic.api")
        print("  • Maven dependencies: com.github.awitkowski0:minecraftPlayerAnimatorSynced → bond.thematic:thematic-api")
        print("  • Mod IDs: playeranimator/themAnimator → thematic-api")
        print("  • Configuration files: Updated package references")
        
        print("\n⚠️  Manual steps you may need to complete:")
        print("  1. Update your build.gradle to use JitPack repository:")
        print("     repositories {")
        print("         maven { url 'https://jitpack.io' }")
        print("     }")
        print("  2. Update dependency version to latest thematic-api version")
        print("  3. Test your mod to ensure everything works correctly")
        print("  4. Update any hardcoded strings that reference the old mod name")
        print("  5. Update your mod's documentation and README files")
        
        if self.dry_run:
            print(f"\n🔍 This was a DRY RUN - no files were actually modified!")
            print("   Run without --dry-run to apply changes.")

def main():
    parser = argparse.ArgumentParser(
        description="Migrate your Minecraft mod to use the new Thematic API package structure",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python migrate_to_thematic_api.py ./src
  python migrate_to_thematic_api.py /path/to/my/mod/src --dry-run --verbose
  python migrate_to_thematic_api.py . --verbose

Note: This script will create a backup of your source code before making changes.
        """
    )
    
    parser.add_argument(
        'source_path',
        help='Path to your mod\'s source directory (usually ./src or ./minecraft)'
    )
    
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Preview changes without modifying files'
    )
    
    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help='Show detailed output for each file processed'
    )
    
    args = parser.parse_args()
    
    source_path = Path(args.source_path).resolve()
    
    try:
        migrator = ThematicAPIMigrator(dry_run=args.dry_run, verbose=args.verbose)
        stats = migrator.migrate(source_path)
        migrator.print_migration_report(stats, source_path)
        
        if stats['total_changes'] > 0:
            print(f"\n✅ Migration completed successfully!")
            if not args.dry_run:
                print("   Your mod has been updated to use Thematic API.")
                print("   Don't forget to test your mod and complete any manual steps listed above.")
        else:
            print(f"\n ℹ️ No changes needed - your mod may already be using the correct package structure.")
        
    except Exception as e:
        print(f"\n❌ Migration failed: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()