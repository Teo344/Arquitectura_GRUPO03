# Actualiza los tres índices del manual con Word, guarda, exporta a PDF y quita w:updateFields.
# Uso: pwsh -File actualizar_indices.ps1 -Docx "C:\ruta\MANUAL.docx" [-Pdf]
param(
    [Parameter(Mandatory = $true)][string]$Docx,
    [switch]$Pdf
)
$Docx = (Resolve-Path $Docx).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    $doc = $word.Documents.Open($Docx)
    # Dos pasadas: la primera fija la paginación, la segunda corrige los números de página
    foreach ($i in 1..2) { foreach ($toc in $doc.TablesOfContents) { $toc.Update() } }
    "Entradas por indice: " + (($doc.TablesOfContents | ForEach-Object { $_.Range.Paragraphs.Count }) -join ' / ')
    "Paginas: " + $doc.ComputeStatistics(2)
    $doc.Save()
    if ($Pdf) { $doc.ExportAsFixedFormat([IO.Path]::ChangeExtension($Docx, '.pdf'), 17) }
    $doc.Close()
} finally {
    $word.Quit()
}

# Quitar w:updateFields para que Word no pregunte al abrir
Add-Type -AssemblyName System.IO.Compression
$zip = [IO.Compression.ZipFile]::Open($Docx, 'Update')
try {
    $entrada = $zip.GetEntry('word/settings.xml')
    $lector = New-Object IO.StreamReader($entrada.Open())
    $xml = $lector.ReadToEnd(); $lector.Close()
    $nuevo = $xml -replace '<w:updateFields[^>]*/>', ''
    if ($nuevo -ne $xml) {
        $entrada.Delete()
        $escritor = New-Object IO.StreamWriter($zip.CreateEntry('word/settings.xml').Open(), (New-Object Text.UTF8Encoding $false))
        $escritor.Write($nuevo); $escritor.Close()
    }
} finally {
    $zip.Dispose()
}
"Listo: $Docx"
