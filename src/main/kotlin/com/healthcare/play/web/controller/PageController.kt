package com.healthcare.play.web.controller

import com.healthcare.play.domain.game.GameType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping(produces = ["text/html"])
class PageController {

    @GetMapping("/", "/index")
    fun home(model: Model): String {
        model.addAttribute("pageTitle", "홈")
        return "index"                 // -> templates/index.html
    }

    @GetMapping("/login")
    fun login(@RequestParam(name = "next", required = false) next: String?, model: Model): String {
        model.addAttribute("pageTitle", "로그인")
        model.addAttribute("next", next ?: "")
        return "login"                 // -> templates/login.html
    }

    @GetMapping("/games")
    fun games(model: Model): String {
        model.addAttribute("pageTitle", "게임")
        return "games/index"           // -> templates/games/index.html
    }

    @GetMapping("/play")
    fun playPage(@RequestParam type: GameType, model: Model): String{
        model.addAttribute("gameType", type)
        return "games/play"
    }

    @GetMapping("/attendance")
    fun attendance(model: Model): String {
        model.addAttribute("pageTitle", "출석")
        return "attendance/index"      // -> templates/attendance/index.html
    }

    @GetMapping("/reports")
    fun reports(model: Model): String {
        model.addAttribute("pageTitle", "리포트")
        return "reports/index"         // -> templates/reports/index.html
    }

    @GetMapping("/subs")
    fun subs(model: Model): String {
        model.addAttribute("pageTitle", "구독")
        return "subs/index"            // -> templates/subs/index.html
    }
}