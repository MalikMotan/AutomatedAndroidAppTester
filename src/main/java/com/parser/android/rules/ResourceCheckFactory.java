package com.parser.android.rules;

import com.parser.android.constants.Constant;
import com.parser.android.model.SimpleClassModel;

public class ResourceCheckFactory {
	
	public ResourceCheck getResourceCheckRequired(String resource, SimpleClassModel model){
		
		switch(resource) 
        { 
            case Constant.CAMERA			:   return new CameraCheck(model);
            case Constant.EXTERNAL_DRIVE	:	return new DriveCheck(model);
            case Constant.LOCATION			: 	return new LocationCheck(model);
            case Constant.CAMERA2 			: 	return new CameraApi2Check(model);
            default							: 	return null;
        } 
    }  
}  