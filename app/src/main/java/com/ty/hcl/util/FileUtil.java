package com.ty.hcl.util;

import java.io.File;

public class FileUtil {
	
	/**
	 * 删除文件（包括子文件）
	 * @param file
	 */
	public static void delete(File file){
		if(file.isDirectory()){//
			File[] childs=file.listFiles();
			for(File file2:childs)
				delete(file2);
		}
		file.delete();//删除此抽象路径名表示的文件或目录。如果此路径名表示一个目录，则该目录必须为空才能删除。
	}
	
	/**
	 * 获得文件后缀名
	 * @param myFile
	 * @return
	 */
	public static String getExtension(String fileName){
		return fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());//注意加一
	}

}
