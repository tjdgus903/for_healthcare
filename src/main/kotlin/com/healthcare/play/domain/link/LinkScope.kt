package com.healthcare.play.domain.link

enum class LinkScope {
    /** 리포트/출석 등 읽기 권한 */
    VIEW_REPORTS,

    /** (선택) 노트/응원 메시지 쓰기 권한 */
    WRITE_NOTE,

    /** 초대/연결 해지 등 관리 권한 */
    MANAGE_LINK
}