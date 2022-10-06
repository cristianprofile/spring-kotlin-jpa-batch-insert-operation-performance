package com.example.testbulk

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Component
@ConfigurationProperties(prefix = "bulk")
data class ConfigurationBulk(
    var totalObjects: Int = 100,
)


@Entity
data class Book(
    @Id @GeneratedValue var id: Long? = null,
    val name: String = "",
    val price: Int
)

@Repository
interface BookRepository : JpaRepository<Book, Long> {
}


@RestController
@RequestMapping("/book")
class MessageController(val bookRepository: BookRepository,val configurationBulk: ConfigurationBulk,
                        @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size:100}") private val batchSize: Int) {


    val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/batch")
    fun bulkBatchInsert() {
        var start = System.currentTimeMillis()

        val books: List<Book> = (1..configurationBulk.totalObjects).map { Book(name = "Peter $it", price = 6) }

        log.info("Finished creating ${configurationBulk.totalObjects} objects in memory in:${(System.currentTimeMillis() - start) / 1000} millisencond")
        start = System.currentTimeMillis()
        log.info("Inserting ..........Total elements ${configurationBulk.totalObjects} in Batch mode size $batchSize")


        val chunked: List<List<Book>> = books.chunked(batchSize)
        chunked.forEach { bookRepository.saveAll(it) }
        log.info("Finished inserting ${configurationBulk.totalObjects} objects in :${System.currentTimeMillis() - start} milliseconds")

    }

    @GetMapping("/sequential")
    fun sequentialInsert() {
        var start = System.currentTimeMillis()
        val books: List<Book> = (1..configurationBulk.totalObjects).map { Book(name = "Peter $it", price = 6) }

        log.info("Finished creating ${configurationBulk.totalObjects} objects in memory in:${(System.currentTimeMillis() - start) / 1000} milliseconds")
        start = System.currentTimeMillis()
        log.info("Inserting sequential ..........Total elements ${configurationBulk.totalObjects}")
        books.forEach { bookRepository.save(it) }
        log.info("Finished inserting ${configurationBulk.totalObjects} objects in :${System.currentTimeMillis() - start} milliseconds")

    }

}


@SpringBootApplication
class JpaBulkInsertApplication

fun main(args: Array<String>) {
    runApplication<JpaBulkInsertApplication>(*args)
}



