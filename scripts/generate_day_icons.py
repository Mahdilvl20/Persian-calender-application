#!/usr/bin/env python3
import os

def get_digit_path(d, x, y, w, h):
    if d == '0':
        return f"M {x+w/2:.1f},{y+2:.1f} C {x+2:.1f},{y+2:.1f} {x+2:.1f},{y+h-2:.1f} {x+w/2:.1f},{y+h-2:.1f} C {x+w-2:.1f},{y+h-2:.1f} {x+w-2:.1f},{y+2:.1f} {x+w/2:.1f},{y+2:.1f} Z"
    elif d == '1':
        return f"M {x+1:.1f},{y+6:.1f} L {x+w/2:.1f},{y+2:.1f} L {x+w/2:.1f},{y+h-2:.1f} M {x+1:.1f},{y+h-2:.1f} L {x+w-1:.1f},{y+h-2:.1f}"
    elif d == '2':
        return f"M {x+2:.1f},{y+6:.1f} C {x+2:.1f},{y+2:.1f} {x+w-2:.1f},{y+2:.1f} {x+w-2:.1f},{y+7.5:.1f} C {x+w-2:.1f},{y+12:.1f} {x+2:.1f},{y+15:.1f} {x+2:.1f},{y+h-2:.1f} L {x+w-1:.1f},{y+h-2:.1f}"
    elif d == '3':
        return f"M {x+2:.1f},{y+2:.1f} L {x+w-2:.1f},{y+2:.1f} L {x+w/2:.1f},{y+10:.1f} C {x+w:.1f},{y+10:.1f} {x+w:.1f},{y+h-2:.1f} {x+w/2:.1f},{y+h-2:.1f} C {x+2:.1f},{y+h-2:.1f} {x+1:.1f},{y+h-4:.1f} {x+1:.1f},{y+h-6:.1f}"
    elif d == '4':
        return f"M {x+w-3.5:.1f},{y+2:.1f} L {x+2:.1f},{y+14:.1f} L {x+w-1:.1f},{y+14:.1f} M {x+w-3.5:.1f},{y+8:.1f} L {x+w-3.5:.1f},{y+h-2:.1f}"
    elif d == '5':
        return f"M {x+w-2:.1f},{y+2:.1f} L {x+2.5:.1f},{y+2:.1f} L {x+2:.1f},{y+9.5:.1f} C {x+4:.1f},{y+8.5:.1f} {x+w:.1f},{y+8.5:.1f} {x+w:.1f},{y+15.5:.1f} C {x+w:.1f},{y+h-2:.1f} {x+3.5:.1f},{y+h-2:.1f} {x+1:.1f},{y+h-4:.1f}"
    elif d == '6':
        return f"M {x+w-2.5:.1f},{y+3:.1f} C {x+3.5:.1f},{y+3:.1f} {x+2:.1f},{y+10:.1f} {x+2:.1f},{y+15:.1f} C {x+2:.1f},{y+h-2:.1f} {x+w-2:.1f},{y+h-2:.1f} {x+w-2:.1f},{y+14:.1f} C {x+w-2:.1f},{y+10:.1f} {x+2:.1f},{y+10:.1f} {x+2:.1f},{y+15:.1f}"
    elif d == '7':
        return f"M {x+2:.1f},{y+2:.1f} L {x+w-1:.1f},{y+2:.1f} L {x+3.5:.1f},{y+h-2:.1f}"
    elif d == '8':
        return f"M {x+w/2:.1f},{y+10.5:.1f} C {x+2:.1f},{y+10.5:.1f} {x+2:.1f},{y+2:.1f} {x+w/2:.1f},{y+2:.1f} C {x+w-2:.1f},{y+2:.1f} {x+w-2:.1f},{y+10.5:.1f} {x+w/2:.1f},{y+10.5:.1f} Z M {x+w/2:.1f},{y+10.5:.1f} C {x+1:.1f},{y+10.5:.1f} {x+1:.1f},{y+h-2:.1f} {x+w/2:.1f},{y+h-2:.1f} C {x+w-1:.1f},{y+h-2:.1f} {x+w-1:.1f},{y+10.5:.1f} {x+w/2:.1f},{y+10.5:.1f} Z"
    elif d == '9':
        return f"M {x+2.5:.1f},{y+h-3:.1f} C {x+w-3.5:.1f},{y+h-3:.1f} {x+w-2:.1f},{y+12:.1f} {x+w-2:.1f},{y+7:.1f} C {x+w-2:.1f},{y+2:.1f} {x+2:.1f},{y+2:.1f} {x+2:.1f},{y+8:.1f} C {x+2:.1f},{y+12:.1f} {x+w-2:.1f},{y+12:.1f} {x+w-2:.1f},{y+7:.1f}"
    return ""

def generate_foreground_xml(day):
    s = str(day)
    h = 22.0
    y = 52.0
    
    paths = []
    if len(s) == 1:
        d = s[0]
        w = 10.0 if d == '1' else 14.0
        x = 54.0 - w / 2.0
        p = get_digit_path(d, x, y, w, h)
        paths.append(p)
    else:
        d1, d2 = s[0], s[1]
        w1 = 9.0 if d1 == '1' else 12.5
        w2 = 9.0 if d2 == '1' else 12.5
        gap = 3.5
        total_w = w1 + gap + w2
        start_x = 54.0 - total_w / 2.0
        p1 = get_digit_path(d1, start_x, y, w1, h)
        p2 = get_digit_path(d2, start_x + w1 + gap, y, w2, h)
        paths.append(p1)
        paths.append(p2)

    combined_paths = " ".join(paths)

    xml = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Centered within 66dp safe zone (bounds: x 21 to 87, y 21 to 87) -->
    
    <!-- Outer Translucent Glass Card -->
    <path
        android:pathData="M34,26 L74,26 A10,10 0 0,1 84,36 L84,76 A10,10 0 0,1 74,86 L34,86 A10,10 0 0,1 24,76 L24,36 A10,10 0 0,1 34,26 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="24"
                android:startY="26"
                android:endX="84"
                android:endY="86"
                android:type="linear">
                <item android:offset="0.0" android:color="#4DFFFFFF" />
                <item android:offset="1.0" android:color="#1AFFFFFF" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Glass Rim Highlight Stroke -->
    <path
        android:pathData="M34,26 L74,26 A10,10 0 0,1 84,36 L84,76 A10,10 0 0,1 74,86 L34,86 A10,10 0 0,1 24,76 L24,36 A10,10 0 0,1 34,26 Z"
        android:strokeWidth="1.5"
        android:strokeColor="#80FFFFFF"
        android:fillColor="#00000000" />

    <!-- Top Header Bar with Accent Gradient -->
    <path
        android:pathData="M34,26 L74,26 A10,10 0 0,1 84,36 L84,43 L24,43 L24,36 A10,10 0 0,1 34,26 Z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:startX="24"
                android:startY="26"
                android:endX="84"
                android:endY="43"
                android:type="linear">
                <item android:offset="0.0" android:color="#E66366F1" />
                <item android:offset="1.0" android:color="#E638BDF8" />
            </gradient>
        </aapt:attr>
    </path>

    <!-- Calendar Binder Rings -->
    <path
        android:pathData="M39,22 L39,29 M69,22 L69,29"
        android:strokeWidth="3"
        android:strokeColor="#FFFFFF"
        android:strokeLineCap="round" />

    <!-- Big Bold Dynamic Day Number (Day {day}) -->
    <path
        android:pathData="{combined_paths}"
        android:strokeWidth="3.2"
        android:strokeColor="#FFFFFF"
        android:fillColor="#00000000"
        android:strokeLineCap="round"
        android:strokeLineJoin="round" />
</vector>
"""
    return xml

def generate_adaptive_xml(day):
    return f"""<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_fg_day_{day}" />
    <monochrome android:drawable="@drawable/ic_launcher_fg_day_{day}" />
</adaptive-icon>
"""

def main():
    drawable_dir = "app/src/main/res/drawable"
    mipmap_dir = "app/src/main/res/mipmap-anydpi-v26"
    os.makedirs(drawable_dir, exist_ok=True)
    os.makedirs(mipmap_dir, exist_ok=True)

    for day in range(1, 32):
        fg_xml = generate_foreground_xml(day)
        fg_path = os.path.join(drawable_dir, f"ic_launcher_fg_day_{day}.xml")
        with open(fg_path, "w", encoding="utf-8") as f:
            f.write(fg_xml)

        adaptive_xml = generate_adaptive_xml(day)
        icon_path = os.path.join(mipmap_dir, f"ic_launcher_day_{day}.xml")
        with open(icon_path, "w", encoding="utf-8") as f:
            f.write(adaptive_xml)

        round_path = os.path.join(mipmap_dir, f"ic_launcher_round_day_{day}.xml")
        with open(round_path, "w", encoding="utf-8") as f:
            f.write(adaptive_xml)

    print("Successfully generated 31 day icon drawables and adaptive mipmaps!")

if __name__ == "__main__":
    main()
