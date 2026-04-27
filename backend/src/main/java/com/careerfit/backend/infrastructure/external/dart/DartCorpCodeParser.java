package com.careerfit.backend.infrastructure.external.dart;

import com.careerfit.backend.infrastructure.external.dart.dto.CorpCodeItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * DART corpCode.xml 파일을 파싱하여 CorpCodeItem 목록으로 변환하는 컴포넌트.
 *
 * ── DART corpCode.xml 구조 ────────────────────────────────────────────────
 *   ZIP 파일 내부에 CORPCODE.xml 파일이 있고 구조는 아래와 같다:
 *
 *   <?xml version="1.0" encoding="UTF-8"?>
 *   <r>
 *     <list>
 *       <corp_code>00126380</corp_code>
 *       <corp_name>국민은행</corp_name>
 *       <stock_code>105560</stock_code>
 *       <modify_date>20240101</modify_date>
 *     </list>
 *     <list>
 *       <corp_code>01234567</corp_code>
 *       <corp_name>비바리퍼블리카</corp_name>
 *       <stock_code> </stock_code>    ← 비상장은 공백
 *       ...
 *     </list>
 *   </r>
 *
 * ── 왜 JAXB 대신 DOM 파싱을 쓰나 ────────────────────────────────────────
 * Spring Boot 3.x / Java 17에서는 javax.xml.bind(JAXB)가 기본 포함이 아니라
 * 의존성 추가가 필요하다. 반면 DocumentBuilder는 JDK 표준 라이브러리이므로
 * 별도 의존성 없이 사용할 수 있다.
 * corpCode.xml의 구조가 단순(3~4개 필드)하므로 DOM 파싱으로 충분하다.
 */
@Slf4j
@Component
public class DartCorpCodeParser {

    /**
     * corpCode.xml InputStream을 받아 CorpCodeItem 목록으로 파싱한다.
     *
     * @param xmlInputStream ZIP에서 꺼낸 CORPCODE.xml의 InputStream
     * @return 전체 기업 목록 (수만 건)
     *
     * ── InputStream을 파라미터로 받는 이유 ──────────────────────────────
     * ZIP 파일에서 직접 InputStream을 꺼내서 넘기는 방식.
     * 임시 파일을 디스크에 쓰지 않아도 되므로 메모리 효율적.
     */
    public List<CorpCodeItem> parse(InputStream xmlInputStream) {
        List<CorpCodeItem> result = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // XXE(XML External Entity) 공격 방어 설정
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlInputStream);
            doc.getDocumentElement().normalize();

            // <list> 태그 전체를 가져옴
            NodeList listNodes = doc.getElementsByTagName("list");
            for (int i = 0; i < listNodes.getLength(); i++) {
                Element el = (Element) listNodes.item(i);
                CorpCodeItem item = new CorpCodeItem();
                item.setCorpCode(getTagValue("corp_code", el));
                item.setCorpName(getTagValue("corp_name", el));
                item.setStockCode(getTagValue("stock_code", el));
                result.add(item);
            }

            log.info("[DartCorpCodeParser] XML 파싱 완료 — 총 {}개 기업", result.size());
        } catch (Exception e) {
            log.error("[DartCorpCodeParser] XML 파싱 실패: {}", e.getMessage(), e);
        }
        return result;
    }

    /** 주어진 태그명의 텍스트 값을 추출. 없으면 빈 문자열 반환 */
    private String getTagValue(String tagName, Element element) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return "";
        return nodes.item(0).getTextContent().trim();
    }
}
