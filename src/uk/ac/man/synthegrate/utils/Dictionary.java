package uk.ac.man.synthegrate.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Dictionary {
	
	//number of total dictionaries used
	public static int total_dictionaries = 0 ;

	long dimension;
	String filename;
	String filepath;
	
	//datafiller name
	String name;
	
	Long free_chunk_offset = 0l;
	
	boolean used;
	
	public Dictionary(){
		dimension = -1;
		name = "dictionary_"+total_dictionaries;
		total_dictionaries++;
		free_chunk_offset = 0l;
		used = false;
	}

	public long getDimension() {
		
		//if dimension was not set, then read the number of lines from the file
		if (dimension == -1)
		{
			LineNumberReader lnr;
			try {
				lnr = new LineNumberReader(new FileReader(new File(filepath+"/"+filename)));
			
			lnr.skip(Long.MAX_VALUE);
			dimension = lnr.getLineNumber() + 1;//Add 1 because line index starts at 0
			lnr.close();
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return dimension;
	}
	public void setDimension(long dimension) {
		this.dimension = dimension;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (dimension ^ (dimension >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dictionary other = (Dictionary) obj;
		//name will be unique
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	/**
	 * Returns -1 if there is no free chuck that could be used for the needed parameters;
	 * @param new_offset
	 * @param new_size
	 * @return
	 */
	public Long get_free_chunk_offset(Long new_size) {
		
		Long chunk_offset = -1l;
		
		if (new_size+free_chunk_offset<dimension) {
			chunk_offset = free_chunk_offset;
			
			//the used size+1, 1 is the next free value
			free_chunk_offset = Math.min(free_chunk_offset+new_size+1, dimension);
		}
		return chunk_offset;
	}
	
	public boolean check_free_chunk (Long new_size) {
		
		if (new_size+free_chunk_offset<dimension) {
			return true;
		}
		return false;
	}

	
	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	
}
