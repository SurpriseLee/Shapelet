package pers.lxs.timeseries.datasetformatconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author SurpriseLee
 * This code is used to convert URC data set format to Arff format which is the specified format of Weka
 */
public class UCRToArffConverter {
	
	public static void main(String[] args)
	{
		UCRToArffConverter converter = new UCRToArffConverter();
		
		String srcFile = "C:\\Users\\SurpriseLee\\Desktop\\UCR Time Series Classification Archive\\UCR_TS_Archive_2015";
	
		converter.recurse(new File(srcFile));	
		
	}
	
	/**
	 * 
	 * @param filepath
	 */
	public void recurse(File filepath)
	{
		File[] files = filepath.listFiles();
		for(File file : files)
		{
			if(file.isFile())
			{
				if(!file.getName().matches(".*.arff"))
				{
					String dstFilePath = file.getAbsolutePath() + file.getName() + ".arff";
					File dstFile = new File(dstFilePath);
					convert(file, dstFile);
				}
			}
			else if(file.isDirectory())
			{
				recurse(file);
			}
		}	
		
	}
	
	
	/**
	 * 
	 * @param srcFile
	 * @param dstFile
	 */
	public void convert(File srcFile, File dstFile)
	{
		try {
			BufferedReader fin = new BufferedReader(new FileReader(srcFile));
			BufferedWriter fout = new BufferedWriter(new FileWriter(dstFile));
			
			String line = null;
			StringBuffer strBuffer = new StringBuffer();
			
			boolean isHeadInfo = false;
			
			while ((line = fin.readLine()) != null)
			{
				String[] arrs = line.trim().split(",");
				
				// write the head information into destination file
				if(!isHeadInfo)
				{
					strBuffer.append("@relation " + dstFile.getName() + "\r\n\r\n");
					
					for(int i = 0; i < arrs.length - 1; i++)
					{
						strBuffer.append("@attribute attr-" + (i + 1) + " numeric\r\n"); 
					}
					strBuffer.append("@attribute class numeric\r\n\r\n");
					strBuffer.append("@data\r\n");
					isHeadInfo = true;
				}
				
				for(int i = 1; i < arrs.length; i++)
				{
					strBuffer.append(arrs[i] + ",");
				}
				strBuffer.append(arrs[0] + "\r\n");	
			}
			strBuffer.append("\r\n");
			
			fout.write(strBuffer.toString()); // 写入文件
			fin.close();
			fout.close(); // 关闭时会将缓冲区的数据强制写入文件

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
