\# Set creation date to update date



``` powershell

exiftool -m -F -P "-DateTimeOriginal<FileModifyDate" "-CreateDate<FileModifyDate" -overwrite\_original \*.jpg \*.jpeg \*.gif \*.mov \*.GIF \*.bmp

```



\# Set Specific date



``` PowerShell

exiftool "-AllDates=2007:05:05 00:01:00" -overwrite\_original \*.jpg \*.jpeg

```

