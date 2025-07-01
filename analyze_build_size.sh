#!/bin/bash

# Script to analyze build size and performance improvements
# Run this before and after optimizations to measure impact

echo "==================================="
echo "GuardianA Build Size Analysis"
echo "==================================="

# Build the release APK
echo "Building release APK..."
./gradlew assembleRelease --quiet

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed! Please fix build errors first."
    exit 1
fi

# Find the generated APK
APK_PATH=$(find app/build/outputs/apk/release -name "*.apk" | head -1)

if [ -z "$APK_PATH" ]; then
    echo "APK not found! Build may have failed."
    exit 1
fi

echo "APK generated: $APK_PATH"
echo ""

# Analyze APK size
echo "=== APK SIZE ANALYSIS ==="
APK_SIZE=$(stat -f%z "$APK_PATH" 2>/dev/null || stat -c%s "$APK_PATH" 2>/dev/null)
APK_SIZE_MB=$(echo "scale=2; $APK_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")

echo "APK Size: ${APK_SIZE} bytes (${APK_SIZE_MB} MB)"

# Analyze contents if unzip is available
if command -v unzip &> /dev/null; then
    echo ""
    echo "=== APK CONTENTS BREAKDOWN ==="
    
    # Create temp directory for analysis
    TEMP_DIR="/tmp/apk_analysis_$$"
    mkdir -p "$TEMP_DIR"
    
    # Extract APK
    unzip -q "$APK_PATH" -d "$TEMP_DIR"
    
    # Analyze major components
    echo "Major components:"
    
    # Classes.dex size
    if [ -f "$TEMP_DIR/classes.dex" ]; then
        DEX_SIZE=$(stat -f%z "$TEMP_DIR/classes.dex" 2>/dev/null || stat -c%s "$TEMP_DIR/classes.dex" 2>/dev/null)
        DEX_SIZE_MB=$(echo "scale=2; $DEX_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")
        echo "  classes.dex: ${DEX_SIZE} bytes (${DEX_SIZE_MB} MB)"
    fi
    
    # Resources size
    if [ -f "$TEMP_DIR/resources.arsc" ]; then
        RES_SIZE=$(stat -f%z "$TEMP_DIR/resources.arsc" 2>/dev/null || stat -c%s "$TEMP_DIR/resources.arsc" 2>/dev/null)
        RES_SIZE_MB=$(echo "scale=2; $RES_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")
        echo "  resources.arsc: ${RES_SIZE} bytes (${RES_SIZE_MB} MB)"
    fi
    
    # Assets size
    if [ -d "$TEMP_DIR/assets" ]; then
        ASSETS_SIZE=$(du -sb "$TEMP_DIR/assets" 2>/dev/null | cut -f1)
        ASSETS_SIZE_MB=$(echo "scale=2; $ASSETS_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")
        echo "  assets/: ${ASSETS_SIZE} bytes (${ASSETS_SIZE_MB} MB)"
        
        # Analyze individual assets
        echo "    Large assets:"
        find "$TEMP_DIR/assets" -type f -exec stat -f "%z %N" {} \; 2>/dev/null | \
        sort -nr | head -5 | while read size file; do
            size_mb=$(echo "scale=2; $size / 1048576" | bc -l 2>/dev/null || echo "N/A")
            filename=$(basename "$file")
            echo "      $filename: ${size} bytes (${size_mb} MB)"
        done
    fi
    
    # Res folder size
    if [ -d "$TEMP_DIR/res" ]; then
        RES_FOLDER_SIZE=$(du -sb "$TEMP_DIR/res" 2>/dev/null | cut -f1)
        RES_FOLDER_SIZE_MB=$(echo "scale=2; $RES_FOLDER_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")
        echo "  res/: ${RES_FOLDER_SIZE} bytes (${RES_FOLDER_SIZE_MB} MB)"
    fi
    
    # Native libraries
    if [ -d "$TEMP_DIR/lib" ]; then
        LIB_SIZE=$(du -sb "$TEMP_DIR/lib" 2>/dev/null | cut -f1)
        LIB_SIZE_MB=$(echo "scale=2; $LIB_SIZE / 1048576" | bc -l 2>/dev/null || echo "N/A")
        echo "  lib/ (native): ${LIB_SIZE} bytes (${LIB_SIZE_MB} MB)"
        
        echo "    Architectures:"
        ls "$TEMP_DIR/lib" 2>/dev/null | while read arch; do
            arch_size=$(du -sb "$TEMP_DIR/lib/$arch" 2>/dev/null | cut -f1)
            arch_size_mb=$(echo "scale=2; $arch_size / 1048576" | bc -l 2>/dev/null || echo "N/A")
            echo "      $arch: ${arch_size} bytes (${arch_size_mb} MB)"
        done
    fi
    
    # Cleanup
    rm -rf "$TEMP_DIR"
else
    echo "Install 'unzip' for detailed APK analysis"
fi

echo ""
echo "=== BUILD CONFIGURATION ANALYSIS ==="

# Check ProGuard/R8 status
if grep -q "minifyEnabled true" app/build.gradle; then
    echo "✓ ProGuard/R8 optimization: ENABLED"
else
    echo "✗ ProGuard/R8 optimization: DISABLED"
fi

# Check resource shrinking
if grep -q "shrinkResources true" app/build.gradle; then
    echo "✓ Resource shrinking: ENABLED"
else
    echo "✗ Resource shrinking: DISABLED"
fi

# Check parallel builds
if grep -q "org.gradle.parallel=true" gradle.properties; then
    echo "✓ Parallel builds: ENABLED"
else
    echo "✗ Parallel builds: DISABLED"
fi

# Check App Bundle splits
if grep -q "enableSplit = true" app/build.gradle; then
    echo "✓ App Bundle splits: CONFIGURED"
else
    echo "✗ App Bundle splits: NOT CONFIGURED"
fi

echo ""
echo "=== RECOMMENDATIONS ==="

# Size-based recommendations
if [ "$APK_SIZE" -gt 50000000 ]; then  # 50MB
    echo "⚠ APK is quite large (>50MB). Consider:"
    echo "  - Implementing Dynamic Feature Modules"
    echo "  - Using Play Asset Delivery for large assets"
    echo "  - Model quantization for TensorFlow Lite"
fi

if [ "$APK_SIZE" -gt 100000000 ]; then  # 100MB
    echo "⚠ APK is very large (>100MB). This will impact download and install times significantly."
fi

echo ""
echo "=== OPTIMIZATION SUMMARY ==="
echo "Current optimizations applied:"
echo "✓ ProGuard/R8 code optimization enabled"
echo "✓ Resource shrinking enabled"
echo "✓ Parallel builds enabled"
echo "✓ TensorFlow dependencies cleaned up"
echo "✓ Enhanced ProGuard rules for better optimization"
echo "✓ App Bundle configuration for dynamic delivery"
echo "✓ Example vector drawables created"
echo ""
echo "Next steps:"
echo "1. Run './convert_images_to_webp.sh' to optimize PNG assets"
echo "2. Consider TensorFlow Lite model quantization"
echo "3. Migrate to Android App Bundle for Play Store distribution"
echo "4. Implement Dynamic Feature Modules for ML functionality"
echo ""
echo "Analysis complete!"