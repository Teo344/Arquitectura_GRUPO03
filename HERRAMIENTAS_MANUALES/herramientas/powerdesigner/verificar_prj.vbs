Option Explicit
Dim app, p, i, doc, ws
Set app = CreateObject("PowerDesigner.Application")
For i = app.Models.Count - 1 To 0 Step -1
  app.Models.Item(i).Close 1
Next
Set p = app.OpenModel(WScript.Arguments(0))
WScript.Echo "Proyecto: " & p.Name & "  clase: " & p.ClassName
On Error Resume Next
WScript.Echo "Documentos en el proyecto: " & p.ModelsAndFiles.Count
For i = 0 To p.ModelsAndFiles.Count - 1
  Set doc = p.ModelsAndFiles.Item(i)
  WScript.Echo " - " & doc.Name & " -> " & doc.Location
Next
p.Close 1
Err.Clear
Set ws = app.ActiveWorkspace
ws.Load WScript.Arguments(1)
If Err.Number <> 0 Then
  WScript.Echo "Workspace: no se pudo cargar por COM (" & Err.Description & ")"
Else
  WScript.Echo "Workspace cargado: " & ws.Name & " elementos=" & ws.Children.Count
  ws.Close
End If
