Add-Type -AssemblyName System.Drawing

function Create-PointF([float]$x, [float]$y) {
    return New-Object System.Drawing.PointF($x, $y)
}

function Generate-AppIcon {
    param(
        [int]$size,
        [string]$outputPath,
        [bool]$isRound = $false
    )

    $bitmap = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    # 1. Background gradient: Sunset Coral (#FFA07A) -> Vibrant Rose (#FD79A8) -> Modern Purple (#6C5CE7)
    $c1 = [System.Drawing.Color]::FromArgb(255, 255, 140, 105) # Warm Coral
    $c2 = [System.Drawing.Color]::FromArgb(255, 108, 92, 231)  # Deep Violet
    $rect = New-Object System.Drawing.Rectangle 0, 0, $size, $size
    $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush $rect, $c1, $c2, 50.0

    if ($isRound) {
        $g.FillEllipse($brush, 0, 0, $size, $size)
    } else {
        # Modern Squircle / Rounded rectangle
        $corner = [int]($size * 0.22)
        $path = New-Object System.Drawing.Drawing2D.GraphicsPath
        $d = $corner * 2
        $path.AddArc(0, 0, $d, $d, 180, 90)
        $path.AddArc($size - $d, 0, $d, $d, 270, 90)
        $path.AddArc($size - $d, $size - $d, $d, $d, 0, 90)
        $path.AddArc(0, $size - $d, $d, $d, 90, 90)
        $path.CloseFigure()
        $g.FillPath($brush, $path)
        $path.Dispose()
    }
    $brush.Dispose()

    $scale = [float]$size / 100.0

    # 2. Draw Back Bubble (semi-transparent white - casual chatting & everyday talks)
    $backBubbleBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(140, 255, 255, 255))
    $bRect = New-Object System.Drawing.RectangleF ([float](22.0 * $scale)), ([float](24.0 * $scale)), ([float](38.0 * $scale)), ([float](30.0 * $scale))
    $g.FillEllipse($backBubbleBrush, $bRect)
    
    # tail of back bubble
    $tail1 = [System.Drawing.PointF[]]@(
        (Create-PointF (28.0 * $scale) (48.0 * $scale)),
        (Create-PointF (18.0 * $scale) (58.0 * $scale)),
        (Create-PointF (36.0 * $scale) (52.0 * $scale))
    )
    $g.FillPolygon($backBubbleBrush, $tail1)
    $backBubbleBrush.Dispose()

    # 3. Draw Main Bubble (Crisp White rounded box - meaningful posts & community)
    $mainBubbleBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 255, 255, 255))
    $mCorner = [float](10.0 * $scale)
    $mPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $mX = [float](36.0 * $scale)
    $mY = [float](38.0 * $scale)
    $mW = [float](46.0 * $scale)
    $mH = [float](34.0 * $scale)
    $mD = $mCorner * 2.0
    $mPath.AddArc($mX, $mY, $mD, $mD, 180, 90)
    $mPath.AddArc(($mX + $mW - $mD), $mY, $mD, $mD, 270, 90)
    $mPath.AddArc(($mX + $mW - $mD), ($mY + $mH - $mD), $mD, $mD, 0, 90)
    $mPath.AddArc($mX, ($mY + $mH - $mD), $mD, $mD, 90, 90)
    $mPath.CloseFigure()
    $g.FillPath($mainBubbleBrush, $mPath)
    
    # tail of main bubble
    $tail2 = [System.Drawing.PointF[]]@(
        (Create-PointF (64.0 * $scale) (70.0 * $scale)),
        (Create-PointF (76.0 * $scale) (80.0 * $scale)),
        (Create-PointF (74.0 * $scale) (70.0 * $scale))
    )
    $g.FillPolygon($mainBubbleBrush, $tail2)
    $mainBubbleBrush.Dispose()
    $mPath.Dispose()

    # 4. Text lines inside main bubble (representing diverse contents: casual, deep, informative)
    $penPink = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 255, 110, 140)), ([float](3.2 * $scale))
    $penPink.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penPink.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($penPink, [float](44.0 * $scale), [float](47.0 * $scale), [float](64.0 * $scale), [float](47.0 * $scale))
    $penPink.Dispose()

    $penPurple = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 108, 92, 231)), ([float](3.2 * $scale))
    $penPurple.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penPurple.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($penPurple, [float](44.0 * $scale), [float](55.0 * $scale), [float](74.0 * $scale), [float](55.0 * $scale))
    $penPurple.Dispose()

    $penOrange = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 255, 160, 110)), ([float](3.2 * $scale))
    $penOrange.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penOrange.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($penOrange, [float](44.0 * $scale), [float](63.0 * $scale), [float](56.0 * $scale), [float](63.0 * $scale))
    $penOrange.Dispose()

    # 5. Draw Sparkle / Star (Creative Idea & Useful Tips)
    $starBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 255, 235, 130))
    $starCenterX = [float](80.0 * $scale)
    $starCenterY = [float](26.0 * $scale)
    $sRadius = [float](8.5 * $scale)
    $sInner = [float]($sRadius * 0.28)
    
    $starPoints = [System.Drawing.PointF[]]@(
        (Create-PointF $starCenterX ($starCenterY - $sRadius)),
        (Create-PointF ($starCenterX + $sInner) ($starCenterY - $sInner)),
        (Create-PointF ($starCenterX + $sRadius) $starCenterY),
        (Create-PointF ($starCenterX + $sInner) ($starCenterY + $sInner)),
        (Create-PointF $starCenterX ($starCenterY + $sRadius)),
        (Create-PointF ($starCenterX - $sInner) ($starCenterY + $sInner)),
        (Create-PointF ($starCenterX - $sRadius) $starCenterY),
        (Create-PointF ($starCenterX - $sInner) ($starCenterY - $sInner))
    )
    $g.FillPolygon($starBrush, $starPoints)
    $starBrush.Dispose()

    # Small sparkle on the top-left
    $smallSparkleBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(220, 255, 255, 255))
    $g.FillEllipse($smallSparkleBrush, [float](26.0 * $scale), [float](18.0 * $scale), [float](4.0 * $scale), [float](4.0 * $scale))
    $smallSparkleBrush.Dispose()

    # Save output
    $dir = Split-Path $outputPath
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bitmap.Dispose()
    Write-Output "Successfully generated: $outputPath ($size x $size)"
}

$baseDir = "d:\컴퓨터 수요\새 폴더\app\src\main\res"

$sizes = @(
    @{ Name = "mipmap-mdpi"; Size = 48 },
    @{ Name = "mipmap-hdpi"; Size = 72 },
    @{ Name = "mipmap-xhdpi"; Size = 96 },
    @{ Name = "mipmap-xxhdpi"; Size = 144 },
    @{ Name = "mipmap-xxxhdpi"; Size = 192 }
)

foreach ($item in $sizes) {
    $sqPath = Join-Path $baseDir "$($item.Name)\ic_launcher.png"
    $rdPath = Join-Path $baseDir "$($item.Name)\ic_launcher_round.png"
    Generate-AppIcon -size $item.Size -outputPath $sqPath -isRound $false
    Generate-AppIcon -size $item.Size -outputPath $rdPath -isRound $true
}

# 512x512 Play Store / Web icon
$webIconPath = "d:\컴퓨터 수요\새 폴더\ic_launcher-web.png"
Generate-AppIcon -size 512 -outputPath $webIconPath -isRound $false
