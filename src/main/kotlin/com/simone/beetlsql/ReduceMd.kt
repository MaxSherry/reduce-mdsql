package com.simone.beetlsql

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Component
open class ReduceMd {
	/**md所在的路径*/
	@Value("\${rootmd}")
	lateinit var rootmd: String
	/**md的sql语句列表 文件名.sqlid*/
	val sqlSource: MutableList<Pair<String, File>> = mutableListOf()
	
	/**
	 * 读取md文件并转成sql列表
	 */
	fun initSqlSource() {
		val list = File(rootmd).walk().filter { it.isFile }.flatMap { f ->
			val readLines = f.readLines()
			readLines.withIndex().filter { it.value.startsWith("===") }.map { "${f.nameWithoutExtension}.${readLines[it.index - 1].trim()}" to f }.asSequence()
		}.toList()
		sqlSource.addAll(list)
	}
	
	/**
	 * 扫描扩展BaseMapper类方式用到的sql
	 */
	fun scanJavaSourceBaseMapper(): List<Pair<String, File>> {
		val list = File(rootmd).parentFile.parentFile.walk().filter { it.isFile && it.name.endsWith(".java") }.map { it to it.readText() }.filter {
			it.second.contains("""\s+extends\s+BaseMapper<\w+>\s+\{""".toRegex(RegexOption.IGNORE_CASE))
		}.toList()
		val contentSqlList = sqlSource.map { sql ->
			list.find {
				it.second.contains("<${sql.first.substringBefore(".")}>",
				                   true) && it.second.contains("""\s+${sql.first.substringAfter(".")}\s*\(""".toRegex(
						RegexOption.IGNORE_CASE))
			}?.run { sql }
		}.filterNotNull()
		
		
		return contentSqlList
	}
	
	/**
	 * 扫描直接调用 md文件名.sqlid 方式用到的sql
	 */
	fun scanJavaSourceModule(): List<Pair<String, File>> {
		val list = File(rootmd).parentFile.parentFile.walk().filter { it.isFile && it.name.endsWith(".java") }.map { it to it.readText() }.toList()
		val contentSqlList = sqlSource.map { sql -> list.find { it.second.contains(""""${sql.first}"""", true) }?.run { sql } }.filterNotNull()
		
		return contentSqlList
	}
	
	@PostConstruct
	fun init() {
		initSqlSource()
		
		println("=============以下是无用的sql================")
		sqlSource.minus(scanJavaSourceBaseMapper()).minus(scanJavaSourceModule()).filterNot {
			//剔除文件内的引用
			it.second.readText().contains("""#use\("${it.first.substringAfter('.')}"|'${it.first.substringAfter('.')}'\)#""".toRegex(RegexOption.IGNORE_CASE))
		}.forEach(::println)
		println("==================end====================")
	}
}