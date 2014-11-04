package denfinder.controllers;

import denfinder.model.*;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller
public class MainController {
	@RequestMapping("/")
	public String home(Model model) {
        model.addAttribute("formData", new FormData());
		return "index";
	}
	
	@RequestMapping("/map")
	public String map() {
		return "map";
	}

    @RequestMapping(value="/results", method=RequestMethod.GET)
    public String results(@ModelAttribute FormData formData, Model model) throws IOException {
    	
    	Pair<Coordinates, Coordinates> viewport = 
    						GeocodingApi.getCoordinates(formData.getAddress());
    	
    	System.out.println(viewport.getLeft().getLatitude() + ", " + viewport.getLeft().getLongitude());
    	System.out.println(viewport.getRight().getLatitude() + ", " + viewport.getRight().getLongitude());
    	
    	
    	
        model.addAttribute("formData", formData);
        //Coordinates coords = GeocodingApi.getCoordinates(formData.getAddress());
        //String fips = FccApi.getFIPSCode(coords);
        //model.addAttribute("censusData", new CensusApi(fips));
        //model.addAttribute("educationData", new Education(coords));
        return "results";
    }
}