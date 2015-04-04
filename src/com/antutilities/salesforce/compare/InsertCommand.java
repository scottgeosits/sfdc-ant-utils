package com.antutilities.salesforce.compare;

import com.antutilities.salesforce.compare.EditCommand;
import com.antutilities.salesforce.compare.FileInfo;

/**
 * Insert a block new lines into the old file.
 */
public class InsertCommand extends EditCommand
{
    public InsertCommand(FileInfo oldFileInfo, FileInfo newFileInfo)
    {
        super( oldFileInfo, newFileInfo );
        command = "Insert before";
        oldLines = oldFileInfo.getBlockAt( oldFileInfo.lineNum );
        newLines = newFileInfo.nextBlock();
        newLines.reportable = true;
    }
}
