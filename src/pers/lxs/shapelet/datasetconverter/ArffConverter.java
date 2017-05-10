package pers.lxs.shapelet.datasetconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pers.lxs.shapelet.Utils;

/**
 * @author SurpriseLee
 * This code is used to convert URC data set format to Arff format which is the specified format of Weka
 */
public class ArffConverter {
	
	public static void main(String[] args)
	{
		ArffConverter converter = new ArffConverter();
		
		String srcFile = "C:\\Users\\SurpriseLee\\Desktop\\UCR Time Series Classification Archive\\UCR_TS_Archive_2015";
	
		System.out.println("Deleting old *.arff files...");
		converter.removeFile(new File(srcFile), ".arff");
			
		System.out.println("Converting files...");
		converter.convert(new File(srcFile), 0);	
		
		System.out.println("Fished!");;
	}
	
	/**
	 * convert data set into arff format
	 * @param relationName
	 * @param data
	 * @param classLabelIndex
	 * @return
	 */
	public String convertToArff(String relationName, List<double[]> data, int classLabelIndex) {
		StringBuffer arffBuffer = new StringBuffer();
		
		Set<Double> set = new HashSet<Double>();
		for(int i = 0; i < data.size(); i++) {
			set.add(data.get(i)[classLabelIndex]);
		}
		
		String headInfo = getHeadInfo(relationName, data.get(0).length, classLabelIndex, set);
		arffBuffer.append(headInfo + "\n");
		String dataContent = getDataContent(data);
		arffBuffer.append(dataContent + "\n");		
		
		return arffBuffer.toString();
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
	public void convert(File filepath, int classLabelIndex)
	{
		if(filepath.exists() && filepath.isFile())
		{
			if(!filepath.getName().matches(".*.arff"))
			{
				String dstFilePath = filepath.getAbsolutePath() + ".arff";
				File dstFile = new File(dstFilePath);
				convertSingleFile(filepath, dstFile, classLabelIndex);
			}
		}
		else if(filepath.exists() && filepath.isDirectory())
		{
			File[] files = filepath.listFiles();
			
			for(File file : files)
			{
				convert(file, classLabelIndex);				
			}
		}		
	}
	
	
	/**
	 * 
	 * @param srcFile
	 * @param dstFile
	 */
	private void convertSingleFile(File srcFile, File dstFile, int classLabelIndex)
	{
		try {
			BufferedReader fin = new BufferedReader(new FileReader(srcFile));
			
			String line = null;
			
			List<double[]> data = new ArrayList<double[]>();
			
			while ((line = fin.readLine()) != null)
			{
				String[] arrs = line.trim().split(",");
				
				double[] sample = new double[arrs.length];
				
				for(int i = 0; i < arrs.length; i++)
				{
					sample[i] = Double.parseDouble(arrs[i]);
				}	
				data.add(sample);
			}
			
			String convertedDataset = convertToArff(dstFile.getName(), data, classLabelIndex);
			fin.close();
			Utils.writeFile(convertedDataset, dstFile.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// generate head info of the arff format file
	private String getHeadInfo(String relationName, int dimension, int classLabelIndex, Set<Double> set)
	{
		StringBuffer headBuffer = new StringBuffer();
		headBuffer.append("@relation " + relationName + "\r\n\r\n");
		
		double[] array = setToArray(set);
		bubbleSort(array);
		
		System.out.println(arrayToString(array));
					
		for(int i = 0; i < dimension; i++)
		{
			if(i == classLabelIndex) {
				headBuffer.append("@attribute class " + arrayToString(array) + "\r\n");
			} else {
				headBuffer.append("@attribute attr-" + i + " numeric\r\n"); 
			}
			
		}		
		
		return headBuffer.toString();
	}
	
	private String getDataContent(List<double[]> data) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("@data\r\n");
		for(int i = 0; i < data.size(); i++) {
			buffer.append("" + data.get(i)[0]);
			for(int j = 1; j < data.get(i).length; j++) {
				buffer.append("," + data.get(i)[j]);
			}
			buffer.append("\n");
		}
				
		return buffer.toString();
	}
	
	
	// convert set to array
 	private double[] setToArray(Set<Double> set)
	{
		double[] array = new double[set.size()];
		
		int index = 0;
		for(double element : set)
		{
			array[index++] = element;
		}
		
		return array;
	}
	
	// bubble sort 
	private void bubbleSort(double[] array)
	{
		for(int i = 0; i < array.length - 1; i++)
		{
			for(int j = array.length - 1; j > i; j--)
			{
				if(array[j - 1] > array[j])
				{
					double temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
				}				
			}
		}
	}
	
	// convert array to string
	private String arrayToString(double[] array)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("{" + array[0]);
		
		for(int i = 1; i < array.length; i++)
		{
			buffer.append("," + array[i]);
		}
		
		buffer.append("}");
		return buffer.toString();
	}

}
