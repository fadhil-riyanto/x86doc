x86doc
======
![image](https://user-images.githubusercontent.com/19568093/234743510-51bc85fb-336f-44de-a6c8-cf3e584000a0.png)
![image](https://user-images.githubusercontent.com/19568093/234743611-05f68da4-42cc-43ed-a375-0b855536ec57.png)

How To Run
----------

1. install python, and pdfminer (pip install pdfminer)
2. download intel pdf at: https://software.intel.com/content/www/us/en/develop/articles/intel-sdm.html
   Intel 64 and IA-32 architectures software developer's manual volume 2A: Instruction set reference, A-L
   Intel 64 and IA-32 architectures software developer's manual volume 2B: Instruction set reference, M-U
   Intel 64 and IA-32 architectures software developer's manual volume 2C: Instruction set reference, V-Z
   Intel 64 and IA-32 architectures software developer's manual volume 2D: Instruction set reference, W-Z
3. pdfminer doesn't understand how these are encrypted, so print them to PDF (open in google Chrome, Print -> Save as PDF), both starting only from the first instruction in the document (not the whole document).
4. run 1_extract.bat
5. run 2_makeindex.bat

The set is also available online at [yubsoft.com/x86doc][2].

  [1]: https://software.intel.com/content/www/us/en/develop/articles/intel-sdm.html
  [2]: https://www.yubsoft.com/x86doc/
