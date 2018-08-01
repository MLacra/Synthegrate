package uk.ac.man.synthegrate.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.ac.man.synthegrate.generator.MappingNode;
import uk.ac.man.synthegrate.generator.Synthegrate;
import uk.ac.man.synthegrate.schema_components.Relation;

public class Utils {

	static char[] CHARSET_AZ_09 = "abcdefghijklmnopqrstwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	static int[]  INTSET_09 = {0,1,2,3,4,5,6,7,8,9};
	static char[] CHARSET_AZ = "abcdefghijklmnopqrstwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	static char[] LOWER_CASE_CHARSET_AZ_09 = "abcdefghijklmnopqrstwxyz0123456789".toCharArray();
	static char[] LOWER_CASE_CHARSET_AZ = "abcdefghijklmnopqrstwxyz".toCharArray();
	
	public static String DICTIONARIES_FOLDER_PATH = "";
	
	static long total_attributes =-1;
	
	static long total_relations = -1;
	
	static long total_schemas = -1;
	
	static Random random = new Random();
	
	public static String generateSchemaName() {
		total_schemas++;
		return "schema_"+total_schemas;
	}
	
	public static String generateRelationName() {
	    
//		return "relation_"+generateLowercaseRandomString(3);
		total_relations++;
		return "relation_"+total_relations;
		
	}
	
	public static String generateAttributeName() {
	    total_attributes++;
	    return "attribute_"+total_attributes;
//		return "attribute_"+generateLowercaseRandomString(2);
	}
	
	public static void print_last_names(){
		
		System.out.println("last attribute index ="+total_attributes);
		System.out.println("last relation index ="+total_relations);
		System.out.println("last schema index ="+total_schemas);
	}

	public static String generateRandomString (int length)
	{
		Random random = new SecureRandom();
	    char[] result = new char[length];
	    
	    //set first character as a letter, not a number
	    int randomCharIndex = random.nextInt(CHARSET_AZ.length);
        result[0] = CHARSET_AZ[randomCharIndex];
	    
	    for (int i = 1; i < result.length; i++) {
	        // picks a random index out of character set > random character
	        randomCharIndex = random.nextInt(CHARSET_AZ_09.length);
	        result[i] = CHARSET_AZ_09[randomCharIndex];
	    }
	    return new String(result);
	}
	
	@SuppressWarnings("unused")
	private static String generateLowercaseRandomString (int length)
	{
		Random random = new SecureRandom();
	    char[] result = new char[length];
	    
	    //set first character as a letter, not a number
	    int randomCharIndex = random.nextInt(LOWER_CASE_CHARSET_AZ.length);
        result[0] = LOWER_CASE_CHARSET_AZ[randomCharIndex];
	    
	    for (int i = 1; i < result.length; i++) {
	        // picks a random index out of character set > random character
	        randomCharIndex = random.nextInt(LOWER_CASE_CHARSET_AZ_09.length);
	        result[i] = LOWER_CASE_CHARSET_AZ_09[randomCharIndex];
	    }
	    return new String(result);
	}
	
	public static Document readDOMDocument(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder().parse(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new Error("EXCEPTION when reading DOM Document!: " + e);
		}
	}
    
	public static boolean haveCommonVals (ArrayList<? extends Object> outer, ArrayList<? extends Object> inner){
		
		if (outer.get(0).getClass()!=inner.get(0).getClass())
			   return false;
		
		for (Object o: outer)
			if (inner.contains(o))
				return true;
		
		return false;
	}
	
	public static Object choose_random(ArrayList<? extends Object> array)
	{
		Random random = new SecureRandom();
		int size = array.size();
		
		int index = random.nextInt(size);
		
		return array.get(index);
	}
	
	public static MappingNode most_matches(ArrayList<MappingNode> nodes) {
		
		MappingNode max_node = null;
		int max_matches = 0;
		if (nodes ==null||nodes.isEmpty())
			return max_node;
		
		for (MappingNode node: nodes)
		{
			int matches_size = node.getRelation().getSizeMatchedAttributes();
			if (matches_size>max_matches)
			{
				max_node = node;
				max_matches = matches_size;
			}
		}
			
		
	return max_node;
	}
	
	public static Dictionary choose_random_dictionary(ArrayList<Dictionary> array, long min_size)
	{
		if (array==null)
			return null;
		
		ArrayList<Dictionary> to_choose= new ArrayList<>();
		
		for (Dictionary d: array)
//			if(d.check_free_chunk(min_size))
			if (!d.isUsed())
				to_choose.add(d);
		
		if (!to_choose.isEmpty()) {
			Random random = new SecureRandom();
			int size = to_choose.size();
			int index = random.nextInt(size);
			return to_choose.get(index);
		}else
		{
			Dictionary new_dictionary = create_write_dictionary(Synthegrate.getMax_cardinality());
			array.add(new_dictionary);
			return new_dictionary;
		}
		
		
	}
	
	public static Dictionary create_write_dictionary(long max_size) {
		
		String new_dictionary_name = "dictionary"+Dictionary.total_dictionaries+".txt";
		Set<String> values = new HashSet<>();
		try {
		BufferedWriter writer = new BufferedWriter(new FileWriter(DICTIONARIES_FOLDER_PATH + "/" + new_dictionary_name ));
		
		for (int i=0;i<max_size*2;i++) {
			String value = generateRandomString((int)get_random_index_range(5, 15, true));
			while (values.contains(value)) {
				value = generateRandomString((int)get_random_index_range(5, 15, true));
			}
			writer.write(value+"\n");
		}
		//must avoid the final new line by adding one more entry
		String value = generateRandomString((int)get_random_index_range(5, 15, true));
		while (values.contains(value)) {
			value = generateRandomString((int)get_random_index_range(5, 15, true));
		}
		writer.write(value);
		writer.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Dictionary new_dictionary = readDictionary(new_dictionary_name);
		Constants.addDictionary(new_dictionary);
		return new_dictionary;
	}
	
	
	/**
	 * Generate a number within beginIndex and endIndex. Set if it can be 0 or not. 
 	 * @param beginIndex
	 * @param endIndex
	 * @param not_zero
	 * @return
	 */
	public static long get_random_index_range(long beginIndex, long endIndex , boolean not_zero){
		
		if (beginIndex==endIndex)
			return endIndex;
		
		if (beginIndex>endIndex)
			return 0;
		
		long index = 0;
		
		index = ThreadLocalRandom.current().nextLong(beginIndex,endIndex);
//		System.out.println("index range ["+beginIndex+","+endIndex+"] index="+index);
		if (not_zero) {
			while (index==0) {
//				System.out.println("while loop - index range ["+beginIndex+","+endIndex+"]");
				//exceptional case
				if (beginIndex==0 && endIndex==1)
					index = 1;
				else
					index = ThreadLocalRandom.current().nextLong(beginIndex,endIndex);
			}
		}
		return index;
	}
	
	/**
	 * Returns a random coprime number to the input number. If there are no coprime numbers within the range, then return -1 (we expect all input numbers to be positive).
	 * @param input_number
	 * @param beginIndex
	 * @param endIndex
	 * @return coprime number with input number 
	 */
	public static long get_coprime_number(long input_number, long beginIndex, long endIndex) {
		
		long result = -1l;
		
		if (input_number==0)
			return -1;
		
		if (beginIndex==endIndex)
			if (gcd(input_number,endIndex)==1)
				return endIndex;
			else
				return -1;
		
		if (beginIndex>endIndex)
			return -1;
		
		ArrayList<Long> list = new ArrayList<>();
		
		for (long i = beginIndex;i<=endIndex;i++)
		{
			if (gcd(input_number, i)==1l)
			{
				list.add(i);
			}
		}
		
		if (!list.isEmpty())
		{
			result = (long) choose_random(list);
		}
		
		return result;
	}
	
	/**
	 *  Euclid method for finding the greatest common divisor*/
	private static long gcd(long a, long b) {
	    long t;
	    while(b != 0){
	        t = a;
	        a = b;
	        b = t%b;
	    }
	    return a;
	}
	
	/** 
	 * Check if two numbers are relatively prime */
	public static boolean relativelyPrime(long a, long b) {
	    return gcd(a,b) == 1;
	}
	
	/**
	 * Generates a number between 0 and 1 for the partial inclusion dependencies overlap.
	 * @return
	 */
	public static double get_random_overlap(){
		
		double overlap = ThreadLocalRandom.current().nextDouble(0.0,1.0);
		while(overlap==0.0){
			overlap = ThreadLocalRandom.current().nextDouble(0.0,1.0);
		}
		
		return overlap;
	}
	
	/**
	 * Generates randomly true or false.
	 * @return
	 */
	public static boolean getRandomBoolean() {
	    
	    return random.nextBoolean();
	}
	
	/**
	 * Checks if the first array contains all the elements of the second array. The order of the elements does not matter.
	 * @param outer
	 * @param inner
	 * @return
	 */
 	public static boolean linearIn(ArrayList<? extends Object> outer, ArrayList<? extends Object> inner) {
		
		   if (outer.get(0).getClass()!=inner.get(0).getClass())
			   return false;
		   return outer.containsAll(inner);
		}
	
 	/**
	 * Check if the two arrays have the same elements. The order of the elements does not matter.
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean arraysEqual(ArrayList<? extends Object> array1, ArrayList<? extends Object> array2) {
		
		   if (array1.get(0).getClass()!=array2.get(0).getClass())
			   return false;
		   
		   if (array1.size()!=array2.size())
			   return false;
		   
		   if (array1.containsAll(array2) && array2.containsAll(array1))
			   return true;
		   
		   return false;
		}

	/**
	 * extendible in JOIN
	 * @param extended_node
	 * @return
	 */
	public static boolean is_relation_JOIN_extendible(MappingNode extended_node){
		boolean is_extendible = false;
		Relation extended_relation = extended_node.getRelation();
		long size = extended_relation.getSizeMatchedAttributes();
		
		//don't split a relation which is used in as referenced in a FK relationship
		if (size>1&&(!extended_relation.containsPK()||extended_node.isRoot()))
			is_extendible = true;
		
		return is_extendible;
	}
	
	/**
	 * extendible in UNION 
	 * @param extended_relation
	 * @return
	 */
	public static boolean is_relation_UNION_extendible(Relation extended_relation, boolean ifDisjoint){
		boolean is_extendible = false;
		long size = extended_relation.getCardinality();
		
		if (ifDisjoint){
			if (size>1 )
				is_extendible = true;
		}else
			is_extendible = true;
		
		return is_extendible;
	}
	
	/**
	 * Reads a dictionary file - keep name and dimension. This is used for Datafiller parameters.
	 * @param name
	 * @return
	 */
	public static Dictionary readDictionary (String name){
		Dictionary newDictionary = new Dictionary();
		newDictionary.setFilepath(DICTIONARIES_FOLDER_PATH);
		newDictionary.setFilename(name);
		newDictionary.getDimension();
		System.out.println("dictionary "+name+ " = "+ newDictionary.getDimension()+" words");
		return newDictionary;
		
	}

	/**
	 * Gets the file names from a directory.
	 * @param folderPath
	 * @return
	 */
	public static ArrayList<String> getFileNames(String folderPath) {

		ArrayList<String> file_names = new ArrayList<>();

		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				file_names.add(listOfFiles[i].getName());
			} 
		}
		return file_names;
	}

	/**
	 * Used to compare cell values. The value might be correct, but the format is slightly different.
	 * @param word
	 * @return
	 */
	public static String strip(String word)
	{
		return word.replaceAll("[^A-Za-z0-9]+", "").toLowerCase();
		
	}

}
