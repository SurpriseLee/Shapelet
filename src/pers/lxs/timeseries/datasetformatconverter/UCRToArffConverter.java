package pers.lxs.timeseries.datasetformatconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author SurpriseLee
 * This code is used to convert URC data set format to Arff format which is the specified format of Weka
 */
public class UCRToArffConverter {
	
	public static void main(String[] args)
	{
		UCRToArffConverter converter = new UCRToArffConverter();
		
		String srcFile = "C:\\Users\\SurpriseLee\\Desktop\\UCR Time Series Classification Archive\\UCR_TS_Archive_2015";
	
		System.out.println("Deleting old *.arff files...");
		converter.removeFile(new File(srcFile), ".arff");
			
		System.out.println("Converting files...");
		converter.convert(new File(srcFile));	
		
		System.out.println("Fished!");;
	}
	
	/**
	 * delete files which is satisfied the filter
	 * @param filepath
	 * @param filter
	 */
	public void removeFile(File filepath, String filter)
	{
		if(filepath.exists() && filepath.isFile())
		{
			if(filepath.getName().endsWith(filter))
			{
				filepath.delete();
			}
		}
		else if(filepath.exists() && filepath.isDirectory())
		{
			File[] files = filepath.listFiles();
			for(File file : files)
			{
				removeFile(file, filter);
			}
		}
		
	}
	
	/**
	 * 
	 * @param filepath
	 */
	public void convert(File filepath)
	{
		if(filepath.exists() && filepath.isFile())
		{
			if(!filepath.getName().matches(".*.arff"))
			{
				String dstFilePath = filepath.getAbsolutePath() + ".arff";
				File dstFile = new File(dstFilePath);
				convertSingleFile(filepath, dstFile);
			}
		}
		else if(filepath.exists() && filepath.isDirectory())
		{
			File[] files = filepath.listFiles();
			
			for(File file : files)
			{
				convert(file);				
			}
		}		
	}
	
	
	/**
	 * 
	 * @param srcFile
	 * @param dstFile
	 */
	public void convertSingleFile(File srcFile, File dstFile)
	{
		try {
			BufferedReader fin = new BufferedReader(new FileReader(srcFile));
			BufferedWriter fout = new BufferedWriter(new FileWriter(dstFile));
			
			String line = null;
			
			StringBuffer dataBuffer = new StringBuffer();
			
			Set<Integer> set = new HashSet<Integer>();
			int dimension = 0;
			
			dataBuffer.append("@data\r\n");
			while ((line = fin.readLine()) != null)
			{
				String[] arrs = line.trim().split(",");
				
				dimension = arrs.length - 1;
				
				set.add(Integer.parseInt(arrs[0]));
				dataBuffer.append("C" + arrs[0]);
				for(int i = 1; i < arrs.length; i++)
				{
					dataBuffer.append("," + arrs[i]);
				}
				
				dataBuffer.append("\r\n");
			}
			dataBuffer.append("\r\n");
			
			String headInfo = getHeadInfo(dstFile, dimension, set);
			String convertedDataset = headInfo + "\r\n" + dataBuffer.toString();
			fout.write(convertedDataset); // 写入文件
			fin.close();
			fout.close(); // 关闭时会将缓冲区的数据强制写入文件

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// generate head info of the arff format file
	public String getHeadInfo(File dstFile, int dimension, Set<Integer> set)
	{
		StringBuffer headBuffer = new StringBuffer();
		headBuffer.append("@relation " + dstFile.getName() + "\r\n\r\n");
		
		int[] array = setToArray(set);
		bubbleSort(array);
		
		System.out.println(arrayToString(array));
				
		headBuffer.append("@attribute class " + arrayToString(array) + "\r\n");
		
		for(int i = 1; i < dimension; i++)
		{
			headBuffer.append("@attribute attr-" + i + " numeric\r\n"); 
		}		
		
		return headBuffer.toString();
	}
	
	// convert set to array
 	public int[] setToArray(Set<Integer> set)
	{
		int[] array = new int[set.size()];
		
		int index = 0;
		for(int element : set)
		{
			array[index++] = element;
		}
		
		return array;
	}
	
	// bubble sort 
	public void bubbleSort(int[] array)
	{
		for(int i = 0; i < array.length - 1; i++)
		{
			for(int j = array.length - 1; j > i; j--)
			{
				if(array[j - 1] > array[j])
				{
					int temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
				}				
			}
		}
	}
	
	// convert array to string
	public String arrayToString(int[] array)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("{C" + array[0]);
		
		for(int i = 1; i < array.length; i++)
		{
			buffer.append(", C" + array[i]);
		}
		
		buffer.append("}");
		return buffer.toString();
	}

}
