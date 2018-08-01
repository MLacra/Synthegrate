package uk.ac.man.synthegrate.utils;

import uk.ac.man.synthegrate.generator.ScenarioGenerator;
import uk.ac.man.synthegrate.generator.Synthegrate;

public class Parameters {

	long offset;
	long shift;
	long step;
	long size;
	
	Dictionary dictionary = null;
	
	public Parameters() {
		offset = 0l;
		shift = 0l;
		step = 0l;
		size = 0l;
		dictionary = null;
	}
	
	public Parameters(Parameters other) {
		
		if (other==null)
			return;
		this.offset = other.offset;
		this.shift = other.shift;
		this.step = other.step;
		this.size = other.size;
		this.dictionary = other.dictionary;
	}

	public Parameters(long offset, long shift, long step, long size, Dictionary dictionary) {
		super();
		this.offset = offset;
		this.shift = shift;
		this.step = step;
		this.size = size;
		this.dictionary = dictionary;
	}
	

	public static Parameters generate_random_parameters(long relation_size) {
		
		Parameters new_params = new Parameters();
		Dictionary dictionary = Utils.choose_random_dictionary(Constants.dictionaries,relation_size);
		long dictionary_size = dictionary.getDimension();
		int tries = 0;
		while (!dictionary.check_free_chunk(relation_size)||dictionary.isUsed()) {
			tries++;
			
			if (tries>ScenarioGenerator.tries)
			{
				//create custom dictionary
				dictionary = Utils.create_write_dictionary(Synthegrate.getMax_cardinality());
				dictionary_size = dictionary.getDimension();
				break;
			}
			dictionary = Utils.choose_random_dictionary(Constants.dictionaries,relation_size);
			dictionary_size = dictionary.getDimension();
		}
		dictionary.setUsed(true);
		long offset = dictionary.get_free_chunk_offset(relation_size);
		long shift = 0l;
		long size = dictionary_size-offset-shift;
		long step = 1;
		
		//leave shift as 0l - for the sake of simplicity
		new_params.setOffset(offset);
		new_params.setStep(step);
		new_params.setShift(shift);
		new_params.setSize(size);
		new_params.setDictionary(dictionary);
				
		return new_params;
	}
	
	public static Parameters generate_random_parameters_different_step_values(long relation_size) {
		
		Parameters new_params = new Parameters();
		Dictionary dictionary = Utils.choose_random_dictionary(Constants.dictionaries,2l*relation_size);
		long dictionary_size = dictionary.getDimension();
		long max_step = (long) Math.floor((double)dictionary_size/(double)relation_size);
		
		while (relation_size>dictionary_size) {
			dictionary = Utils.choose_random_dictionary(Constants.dictionaries,2l*relation_size);
			dictionary_size = dictionary.getDimension();
			max_step = (long) Math.floor((double)dictionary_size/(double)relation_size);
		}
		dictionary.setUsed(true);
		long offset = Utils.get_random_index_range(0, max_step-1, false);
		long shift = 0l;//Utils.get_random_index_range(0, max_step);
		long size = dictionary_size-offset-shift;
		long step = Utils.get_coprime_number(size, 1, max_step);
		
		long current_tries = 0;
		while (step==-1||(step==2&&size%2==0)) {
		
		//while (size<1||!Utils.relativelyPrime(size,step)){
			 current_tries++;
//			 step = Utils.get_random_index_range(0, max_step-1, true);
//			 offset = Utils.get_random_index_range(0, max_step, false);
//			 shift = 0l;//Utils.get_random_index_range(0, max_step);
//			 size = dictionary_size-offset-shift;
			 
			offset = Utils.get_random_index_range(0, max_step-1, false);
			shift = 0l;// Utils.get_random_index_range(0, max_step);
			size = dictionary_size-offset-shift;
			step = Utils.get_coprime_number(size, 1, max_step);
			
			 if (current_tries>ScenarioGenerator.tries) {
				 System.err.println("Random parameters were not generated (exceeded number of tries)! Returned null.");
				 return null;
			 }
		}
		
		
		//leave shift as 0l - for the sake of simplicity
		new_params.setOffset(offset);
		new_params.setStep(step);
		new_params.setShift(shift);
		new_params.setSize(size);
		new_params.setDictionary(dictionary);
		
		return new_params;
	}
	
	@Override
	public String toString(){
		return "offset = "+offset+", shift = "+shift +", step = "+step+", size = "+size+", Dictionary = "+dictionary.getName();
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getShift() {
		return shift;
	}

	public void setShift(long shift) {
		this.shift = shift;
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (offset ^ (offset >>> 32));
		result = prime * result + (int) (shift ^ (shift >>> 32));
		result = prime * result + (int) (size ^ (size >>> 32));
		result = prime * result + (int) (step ^ (step >>> 32));
		result = prime * result + (int) (dictionary.hashCode());
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
		Parameters other = (Parameters) obj;
		if (offset != other.offset)
			return false;
		if (shift != other.shift)
			return false;
		if (size != other.size)
			return false;
		if (step != other.step)
			return false;
		if (dictionary != other.dictionary)
			return false;
		return true;
	}
	
	
	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public static long get_partial_ind_offset (Parameters params, long relation_size, long common_vals)
	{
		if (params==null)
			return -1l;
		
		long index_first_common_value = params.offset+(params.shift+params.step*(relation_size-common_vals));
	
		long new_offset = index_first_common_value - params.shift;

		return new_offset;
	}
	

}
