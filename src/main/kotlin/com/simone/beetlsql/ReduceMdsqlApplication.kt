package com.simone.beetlsql

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.io.ClassPathResource

@SpringBootApplication
open class ReduceMdsqlApplication {
	
	/**
	 * 配置文件内容有中文,此处使用UTF-8来读取
	 */
	@Bean
	open fun configurer(): PropertySourcesPlaceholderConfigurer {
		val configurer = PropertySourcesPlaceholderConfigurer()
		configurer.setLocation(ClassPathResource("application.properties"))
		configurer.setFileEncoding("UTF-8")
		return configurer
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(ReduceMdsqlApplication::class.java, *args)
}
