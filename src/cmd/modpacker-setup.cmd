@echo Creating ModPacker script.
@java -cp nwn-tools.jar org.progeeks.util.PathTool @java -cp @cwd@\nwn-tools.jar org.progeeks.nwn.ModPacker %%1 %%2 > ModPacker.cmd
@echo Creating ModUnpacker script.
@java -cp nwn-tools.jar org.progeeks.util.PathTool @java -cp @cwd@\nwn-tools.jar org.progeeks.nwn.ModReader %%1 %%2 > ModUnpacker.cmd
@echo Creating setpath script.
@java -cp nwn-tools.jar org.progeeks.util.PathTool set PATH=%%PATH%%;@cwd@ > setpath.cmd
@echo Done.

