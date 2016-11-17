package com.simone.beetlsql

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Component
open class ReduceMd {
	@Value("\${rootmd}")
	lateinit var rootmd: String
	
	val sqlSource: MutableList<String> = mutableListOf()
	
	
	fun initSqlSource() {
		val list = File(rootmd).walk().filter { it.isFile }.flatMap { f ->
			val readLines = f.readLines()
			readLines.withIndex().filter { it.value.startsWith("===") }.map { "${f.nameWithoutExtension}.${readLines[it.index - 1].trim()}" }.asSequence()
		}.toList()
		sqlSource.addAll(list)
	}
	
	fun scanJavaSourceBaseMapper(): List<String> {
		val list = File(rootmd).parentFile.parentFile.walk().filter { it.isFile && it.name.endsWith(".java") }.map { it to it.readText() }.filter {
			it.second.contains("""\s+extends\s+BaseMapper<\w+>\s+\{""".toRegex(RegexOption.IGNORE_CASE))
		}.toList()
		val contentSqlList = sqlSource.map { sql ->
			list.find {
				it.second.contains("<${sql.substringBefore(".")}>", true) && it.second.contains("""\s+${sql.substringAfter(".")}\s*\(""".toRegex(
						RegexOption.IGNORE_CASE))
			}?.run { sql }
		}.filterNotNull()
		
		
		return contentSqlList
	}
	
	fun scanJavaSourceModule(): List<String> {
		val list = File(rootmd).parentFile.parentFile.walk().filter { it.isFile && it.name.endsWith(".java") }.map { it to it.readText() }.toList()
		val contentSqlList = sqlSource.map { sql -> list.find { it.second.contains(""""$sql"""", true) }?.run { sql } }.filterNotNull()
		
		return contentSqlList
	}
	
	@PostConstruct
	fun init() {
		initSqlSource()
		
		println("=============以下是无用的sql================")
		sqlSource.minus(scanJavaSourceBaseMapper()).minus(scanJavaSourceModule()).forEach(::println)
		println("==================end====================")
	}
}