package com.theorydrivendevelopment.foodadditives;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

public class AdditivesDatabase {
	
	Context context;
	
	public enum Category{
		COLOURS,
		PRESERVATIVES,
		ANTIOXIDANTS,
		STABILISERS,
		REGULATORS,
		ENHANCERS,
		ANTIBIOTICS,
		MISC,
		CHEMICALS,
		NOT_A_CATEGORY
	}
	
	public String getCategoryString(int number){
		Category cat = getCategory(number);
		switch (cat) {
		case COLOURS:
			return context.getString(R.string.catColours);
		case PRESERVATIVES:
			return context.getString(R.string.catPreservatives);
		case ANTIOXIDANTS:
			return context.getString(R.string.catAntioxidants);
		case STABILISERS:
			return context.getString(R.string.catStabilisers);
		case REGULATORS:
			return context.getString(R.string.catRegulators);
		case ENHANCERS:
			return context.getString(R.string.catEnchancers);
		case ANTIBIOTICS:
			return context.getString(R.string.catAntibiotics);
		case MISC:
			return context.getString(R.string.catMisc);
		case CHEMICALS:
			return context.getString(R.string.catChemicals);
		case NOT_A_CATEGORY:
			return context.getString(R.string.catUnknown);
		}
		return "";
	}
	
	public Category getCategory(int number){
		final int catNum = number/100;
		if (catNum > 0 && catNum < 8)
			return Category.values()[catNum-1];
		if (number >= 900 && number <=999)
			return Category.MISC;
		if (number >= 1100 && number <= 1599)
			return Category.CHEMICALS;
		return Category.NOT_A_CATEGORY;
	}
	
	public boolean isDangerous(int addNum){
		if (allAdds.containsKey(addNum))
			return allAdds.get(addNum).isDanger;
		return false;
	}
	public boolean isValid(int addNum) {
		return addNum >= 100 && addNum <1600;
	}
	public boolean containsDescription(int addNum) {
		return _allNumbers.contains(addNum);
	}
	public int nextLeft(int addNum) {
		final int index = _allNumbers.indexOf(addNum);
		if (index != -1 && index != 0) {
			return _allNumbers.get(index-1);
		} else if (index == 0){
			return _allNumbers.get(_allNumbers.size()-1);
		}
		return addNum;
	}
	public int nextRight(int addNum) {
		final int index = _allNumbers.indexOf(addNum);
		if (index != -1 && index != _allNumbers.size()-1) {
			return _allNumbers.get(index+1);
		} else if (index == _allNumbers.size()-1) {
			return _allNumbers.get(0);
		}
		return addNum;
	}
	public int nameToNumber(String name){
		Set<Integer> nameValues = allAdds.keySet();
		for (Integer i : nameValues) {
			if (allAdds.get(i).name == name)
				return i;
		}
		return -1;
	}
	public Additive getDetails(int addNum) {
		if (allAdds.containsKey(addNum))
			return allAdds.get(addNum);
		Additive add = new Additive();
		add.number = addNum;
		add.description = context.getString(R.string.no_details);
		add.name = "E "+String.valueOf(addNum);
		return add;
	}

	public class Additive{
		public int number;
		public String name;
		public String description;
		public boolean isDanger;
	}
	
	private HashMap<Integer, Additive> allAdds;
	
	public AdditivesDatabase(Context context){
		this.context = context;
		this.allAdds = loadAdditives(context.getResources().openRawResource(R.raw.database));
	}
	
	HashMap<Integer, Additive> loadAdditives(InputStream file){
		HashMap<Integer, Additive> adds = new HashMap<Integer,Additive>();
		try{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("string");
		for (int i=0 ; i<nList.getLength() ; ++i){
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;			
				String[] ids = eElement.getAttribute("id").split(",");
				for (int s=0 ; s<ids.length ; ++s) {
					Additive add = new Additive();
					add.number = Integer.parseInt(ids[s].trim());
					add.name = eElement.getAttribute("name") + " (E "+ids[s].trim()+")";
					add.description = eElement.getTextContent();
					add.isDanger = eElement.getAttribute("danger").contains("true");
					adds.put(add.number, add);
				}
			}
		}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return adds;
	}
	
	
	ArrayList<String> dangerList;
	ArrayList<Integer> _allNumbers;
	ArrayList<String> dangerIds(){
		if (dangerList == null){
			dangerList = new ArrayList<String>();
			_allNumbers = new ArrayList<Integer>();
			Set<Integer> nameValues = allAdds.keySet();
			for (Integer i : nameValues) {
				if (allAdds.get(i).isDanger)
					dangerList.add(String.valueOf(i));
				_allNumbers.add(i);
			}
			Collections.sort(dangerList);
			Collections.sort(_allNumbers);
		}
		return dangerList;
	}
	
	ArrayList<String> dangerNames(){
		ArrayList<String> toReturn = new ArrayList<String>();
		Set<Integer> nameValues = allAdds.keySet();
		for (Integer i : nameValues) {
			if (allAdds.get(i).isDanger)
				toReturn.add(allAdds.get(i).name);
		}
		Collections.sort(toReturn,String.CASE_INSENSITIVE_ORDER);
		return toReturn;
	}
	
}
