package org.springblade.log.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class DemoController {
	@GetMapping("info")
	public String info(String name) {
		return "Hello, My Name Is: " + name;
	}
}
