Get-ChildItem -Recurse -LiteralPath 'app\src' -Filter '*.png' | ForEach-Object {
    $bytes = Get-Content -Encoding Byte -TotalCount 4 -LiteralPath $_.FullName
    $sig = ($bytes | ForEach-Object { $_.ToString('X2') }) -join ''
    if ($sig -ne '89504E47') {
        Write-Output ($_.FullName + ' => ' + $sig)
    }
}
