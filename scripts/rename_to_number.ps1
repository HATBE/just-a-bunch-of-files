$folder = "D:\FINAL_UPLOAD\2012-02 Aarons Bilder 2011 und 2012"

Get-ChildItem -Path $folder -Filter *.jpg | ForEach-Object {

    # Extract the last number before .jpg
    if ($_.BaseName -match '(\d+)$') {

        $number = $matches[1]

        # Keep original extension
        $newName = "$number$($_.Extension)"

        Rename-Item -Path $_.FullName -NewName $newName
    }
}