package com.careerfit.backend.infrastructure.external.dart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DART corpCode.xml의 단일 기업 항목 매핑 DTO.
 *
 * DART가 내려주는 XML 구조:
 *   <result>
 *     <list>
 *       <corp_code>00126380</corp_code>
 *       <corp_name>국민은행</corp_name>
 *       <stock_code>105560</stock_code>   ← 상장사는 주식코드 있음, 비상장은 공백
 *       <modify_date>20240101</modify_date>
 *     </list>
 *     ...
 *   </result>
 *
 * ── 왜 JAXB XML 언마샬링을 쓰나 ───────────────────────────────────────────
 * DART corpCode.xml 파일은 수만 건의 기업 목록을 담은 대용량 XML이다.
 * JSON이 아니라 XML 형식으로만 제공되므로 XML 파싱이 필수.
 * Spring Boot 3.x에는 JAXB가 기본 포함되어 있지 않으므로 jakarta.xml.bind 의존성
 * 추가 없이 간단히 쓸 수 있는 방식으로 직접 DOM 파싱을 택했다.
 * (DartCorpCodeParser 클래스 참고)
 */
@Getter
@Setter
@NoArgsConstructor
public class CorpCodeItem {

    /** DART 고유 기업코드 (8자리 숫자 문자열). company.dart_code에 저장됨 */
    private String corpCode;

    /** DART 등록 기업명. 우리 DB의 company.name과 매칭하는 키 */
    private String corpName;

    /**
     * 주식 코드 (상장사만 있음). 비상장사는 공백(" ") 또는 빈 문자열.
     * company.is_public 값 검증에 활용 가능 (지금 당장은 사용 안 함).
     */
    private String stockCode;
}
