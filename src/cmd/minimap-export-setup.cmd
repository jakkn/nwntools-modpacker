@echo Creating MiniMapExport script.
@java -cp nwn-tools.jar org.progeeks.util.PathTool @java -cp @cwd@\nwn-tools.jar;@cwd@\nwn-io.jar org.progeeks.nwn.MiniMapExporter %%* > MiniMapExport.cmd
@echo Creating setpath script.
@java -cp nwn-tools.jar org.progeeks.util.PathTool set PATH=%%PATH%%;@cwd@ > setpath.cmd
@echo Done.

