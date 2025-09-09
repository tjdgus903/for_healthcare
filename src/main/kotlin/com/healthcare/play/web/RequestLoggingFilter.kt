package com.healthcare.play.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val maskHeaders = setOf("authorization", "cookie", "x-user-id")

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val path = req.requestURI
        val method = req.method
        val qs = (req.queryString ?: "").let { if (it.length > 300) it.take(300) + "..." else it }
        val headers = req.headerNames.toList().associateWith { name ->
            val v = req.getHeader(name)
            if (name.lowercase() in maskHeaders) "*****" else v
        }
        log.info("REQ {} {}?{} headers={}", method, path, qs, headers)
        chain.doFilter(req, res)
        log.info("RES {} {} -> {}", method, path, res.status)
    }
}