package pers.lxs.shapelet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
	
	/**
	 * 
	 * @param content
	 * @param srcFile
	 * @param dstFile
	 */
	public static void writeFile(String content, String dstFile) {
		BufferedWriter fout;
		try {
			fout = new BufferedWriter(new FileWriter(dstFile));
			fout.write(content); // 写入文件
			fout.close(); // 关闭时会将缓冲区的数据强制写入文件
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/**
	 * 
	 * @param srcFile
	 * @return
	 */
	public static String readFile(String srcFile) {
		StringBuffer buffer = new StringBuffer();
		
		
		return buffer.toString();
	}

}
