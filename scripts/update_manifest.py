#!/usr/bin/env python3

aliases = []
for day in range(1, 32):
    aliases.append(f"""        <activity-alias
            android:name=".MainActivityAliasDay{day}"
            android:enabled="false"
            android:exported="true"
            android:icon="@mipmap/ic_launcher_day_{day}"
            android:roundIcon="@mipmap/ic_launcher_round_day_{day}"
            android:label="@string/app_name"
            android:targetActivity=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>""")

aliases_str = "\n\n".join(aliases)

manifest_content = f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        
        <!-- Primary Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Dynamic Launcher Icon Aliases (Days 1 to 31) -->
{aliases_str}

        <!-- Dynamic Home Screen Calendar Widget Provider -->
        <receiver
            android:name=".widget.LumaCalendarWidgetProvider"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
                <action android:name="android.intent.action.DATE_CHANGED" />
                <action android:name="android.intent.action.TIME_CHANGED" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
                <action android:name="com.example.widget.UPDATE_LUMA_WIDGET" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/luma_calendar_widget_info" />
        </receiver>

    </application>

</manifest>
"""

with open("app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
    f.write(manifest_content)

print("AndroidManifest.xml successfully updated!")
