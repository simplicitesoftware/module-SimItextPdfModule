![Logo](https://platform.simplicite.io/logos/standard/logo250.png)
* * *

Module legacy PDF export
========================

Provided for backward compatibility.

Instructions
------------

1. Install this module
2. Clear the cache
3. Connect as designer
4. Use the _Legacy PDF document_ module export

Notes
-----

If you imported this module so as to compile old code which was 
using the legacy `PDFTool` helper class you need to refactor this old code
to import and use `com.simplicite.commons.SimItextPdfModule.SpdfTool`
instead of `com.simplicite.util.tools.PDFTool`.

