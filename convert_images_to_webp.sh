#!/bin/bash

# Script to convert PNG images to WebP format for better compression
# This will reduce bundle size significantly while maintaining quality

echo "Converting PNG images to WebP format..."

# Check if cwebp (WebP encoder) is available
if ! command -v cwebp &> /dev/null; then
    echo "cwebp not found. Installing webp tools..."
    # Try to install WebP tools
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y webp
    elif command -v yum &> /dev/null; then
        sudo yum install -y libwebp-tools
    elif command -v brew &> /dev/null; then
        brew install webp
    else
        echo "Please install WebP tools manually: https://developers.google.com/speed/webp/download"
        exit 1
    fi
fi

# Create backup directory
BACKUP_DIR="app/src/main/res/drawable_backup"
mkdir -p "$BACKUP_DIR"

# Convert large PNG files to WebP (files over 10KB)
find app/src/main/res -name "*.png" -size +10k | while read -r file; do
    # Skip if it's already a backup
    if [[ "$file" == *"backup"* ]]; then
        continue
    fi
    
    # Get file info
    filename=$(basename "$file" .png)
    directory=$(dirname "$file")
    backup_file="$BACKUP_DIR/$(basename "$file")"
    webp_file="$directory/$filename.webp"
    
    # Create backup
    cp "$file" "$backup_file"
    echo "Backed up: $file -> $backup_file"
    
    # Convert to WebP with high quality (80% quality, good balance)
    cwebp -q 80 "$file" -o "$webp_file"
    
    if [ $? -eq 0 ]; then
        # Get original and new file sizes
        original_size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
        new_size=$(stat -f%z "$webp_file" 2>/dev/null || stat -c%s "$webp_file" 2>/dev/null)
        
        # Calculate compression ratio
        if [ "$original_size" -gt 0 ]; then
            compression_ratio=$(echo "scale=1; (($original_size - $new_size) * 100) / $original_size" | bc -l 2>/dev/null || echo "N/A")
            echo "Converted: $file -> $webp_file"
            echo "  Original: ${original_size} bytes"
            echo "  New: ${new_size} bytes"
            echo "  Savings: ${compression_ratio}%"
            
            # Remove original PNG if WebP is significantly smaller
            if [ "$new_size" -lt "$original_size" ]; then
                rm "$file"
                echo "  Removed original PNG file"
            else
                echo "  WebP file is not smaller, keeping PNG"
                rm "$webp_file"
            fi
        fi
    else
        echo "Failed to convert: $file"
        rm -f "$backup_file"
    fi
    
    echo "---"
done

echo "Conversion complete!"
echo "Backup files are stored in: $BACKUP_DIR"
echo ""
echo "Note: You may need to update your drawable references in code/XML to use .webp extensions"
echo "Android Studio can help with this by using 'Convert to WebP' feature."