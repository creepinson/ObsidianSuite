package MCEntityAnimator.animation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import MCEntityAnimator.MCEA_Main;
import MCEntityAnimator.Util;
import MCEntityAnimator.render.objRendering.EntityObj;
import MCEntityAnimator.render.objRendering.ModelObj;
import MCEntityAnimator.render.objRendering.RenderObj;
import MCEntityAnimator.render.objRendering.parts.Part;
import MCEntityAnimator.render.objRendering.parts.PartObj;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Model parts can have names assigned to them as blender gives them names like part.001.
 * Parts can also be grouped together, eg upper and lower arm -> arm.
 * The order of the parts is also flexible.
 */
public class PartGroupsAndNames 
{
	private ModelObj model;
	//A mapping of group names to a list of parts in that group.
	private Map<String, List<PartObj>> groups;
	
	public PartGroupsAndNames(ModelObj modelObj)
	{
		this.model = modelObj;
		
		groups = new HashMap<String, List<PartObj>>();
		
		addGroup("Default");
		for(PartObj part : model.getPartObjs())
		{
			setPartGroup("Default", part);
		}
	}
	
	/**
	 * @return A list of the groups.
	 */
	public String[] getGroupListAsArray()
	{
		String[] strArr = new String[groups.keySet().size()];
		int i = 0;
		for(String group : groups.keySet())
		{
			strArr[i] = group;
			i++;
		}
		return strArr;
	}
	
	/**
	 * @return A csv string of the groups.
	 */
	public String getGroupListAsString()
	{
		String list = "";
		for(String group : groups.keySet())
		{
			list += group + ", ";
		}
		if(list.length() > 0)
			list = list.substring(0, list.length() - 2);
		return list;
	}
	
	/**
	 * Add a new group to the group map if it is unique. 
	 * Will assign an empty array list to the group name.
	 * @param groupName The new name of the group to be added.
	 */
	public void addGroup(String groupName)
	{
		if(!groups.keySet().contains(groupName))
			groups.put(groupName, new ArrayList<PartObj>());
	}
	
	public void setPartGroup(String groupName, PartObj part)
	{
		if(groups.containsKey(groupName))
		{
			//Remove part from existing group
			for(String group : groups.keySet())
			{
				List<PartObj> currentParts = groups.get(group);
				if(currentParts.contains(part))
					currentParts.remove(part);
				groups.put(group, currentParts);
			}
			
			//Add part to new group.
			List<PartObj> currentParts = groups.get(groupName);
			currentParts.add(part);
			groups.put(groupName, currentParts);
		}	
	}
	
	public String getPartGroup(PartObj part)
	{
		for(Entry<String, List<PartObj>> s : groups.entrySet())
		{
			if(s.getValue().contains(part))
				return s.getKey();
		}
		return "Default";
	}
	
	public void changeOrder(Part part, boolean up)
	{
		int currentPos = 0;
		for(int i = 0; i < model.parts.size(); i++)
		{
			if(model.parts.get(i).equals(part))
				currentPos = i;
		}
		boolean flag = true;
		if(currentPos == 0 && up)
			flag = false;
		if(currentPos == model.parts.size() - 1 && !up)
			flag = false;
		if(flag)
		{				
			model.parts.remove(part);
			if(up)
				model.parts.add(currentPos - 1, part);
			else
				model.parts.add(currentPos + 1, part);
		}
	}
	
	public NBTTagCompound getSaveData(String entityName) 
	{	
		NBTTagCompound nbtToReturn = new NBTTagCompound();
		NBTTagList partList = new NBTTagList();
		for(PartObj part : model.getPartObjs())
		{
			NBTTagCompound partCompound = new NBTTagCompound();
			partCompound.setString("Name", part.getName());
			partCompound.setString("DisplayName", part.getDisplayName());
			partCompound.setString("Group", getPartGroup(part));
			partList.appendTag(partCompound);
		}
		nbtToReturn.setTag("GroupsAndNames", partList);
		nbtToReturn.setString("PartOrder", model.getPartOrderAsString());
		return nbtToReturn;
	}
	
	public void loadData(NBTTagCompound compound, String entityName) 
	{	
		NBTTagList partList = compound.getTagList("GroupsAndNames", 10);
		EntityObj entity = new EntityObj(Minecraft.getMinecraft().theWorld, entityName);
		if(entity != null)
		{
			ModelObj entityModel = ((RenderObj) RenderManager.instance.getEntityRenderObject(entity)).getModel(entityName);
			entityModel.setPartOrderFromString(compound.getString("PartOrder"));
			for (int i = 0; i < partList.tagCount(); i++)
			{
				NBTTagCompound partCompound = partList.getCompoundTagAt(i);
				PartObj part = Util.getPartObjFromName(partCompound.getString("Name"), entityModel.parts);
				part.setDisplayName(partCompound.getString("DisplayName"));
				String group = partCompound.getString("Group");
				if(!groups.containsKey(group))
					addGroup(group);
				setPartGroup(group, part);
			}
		}
	}

	
}
