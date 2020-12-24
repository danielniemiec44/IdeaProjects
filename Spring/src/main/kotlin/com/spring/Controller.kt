package com.spring

import org.springframework.web.bind.annotation.*
import javax.persistence.GeneratedValue

@RestController
class BookController(val repo : StudentRepositories) {

    @GetMapping("/students/add/index:{index}&name:{name}&surname:{surname}")
    fun getStudentByIndex(@PathVariable("index") index: String, @PathVariable("name") name: String, @PathVariable("surname") surname: String) {
        repo.save(Student(index, name, surname))
    }

    @GetMapping("/students")
    fun getAllStudents() : List<Student> {
        return repo.findAll().toList()
    }

    @GetMapping("/student/{index}")
    fun getStudentByIndex(@PathVariable("index") index: String) = repo.findByIndex(index)

    @GetMapping("/students/remove/{index}")
    fun removeStudentByIndex(@PathVariable("index") index: String) = repo.delete(repo.findByIndex(index))
}