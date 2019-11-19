package io.vertx.mysqlclient.impl;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL collation which is a set of rules for comparing characters in a character set.
 */
public enum MySQLCollation {
  big5_chinese_ci("big5", "Big5", 1),
  latin2_czech_cs("latin2", "ISO8859_2", 2),
  dec8_swedish_ci("dec8", "Cp1252", 3),
  cp850_general_ci("cp850", "Cp850", 4),
  latin1_german1_ci("latin1", "Cp1252", 5),
  hp8_english_ci("hp8", "Cp1252", 6),
  koi8r_general_ci("koi8r", "KOI8_R", 7),
  latin1_swedish_ci("latin1", "Cp1252", 8),
  latin2_general_ci("latin2", "ISO8859_2", 9),
  swe7_swedish_ci("swe7", "Cp1252", 10),
  ascii_general_ci("ascii", "US-ASCII", 11),
  ujis_japanese_ci("ujis", "EUC_JP", 12),
  sjis_japanese_ci("sjis", "SJIS", 13),
  cp1251_bulgarian_ci("cp1251", "Cp1251", 14),
  latin1_danish_ci("latin1", "Cp1252", 15),
  hebrew_general_ci("hebrew", "ISO8859_8", 16),
  tis620_thai_ci("tis620", "TIS620", 18),
  euckr_korean_ci("euckr", "EUC_KR", 19),
  latin7_estonian_cs("latin7", "ISO-8859-13", 20),
  latin2_hungarian_ci("latin2", "ISO8859_2", 21),
  koi8u_general_ci("koi8u", "KOI8_R", 22),
  cp1251_ukrainian_ci("cp1251", "Cp1251", 23),
  gb2312_chinese_ci("gb2312", "EUC_CN", 24),
  greek_general_ci("greek", "ISO8859_7", 25),
  cp1250_general_ci("cp1250", "Cp1250", 26),
  latin2_croatian_ci("latin2", "ISO8859_2", 27),
  gbk_chinese_ci("gbk", "GBK", 28),
  cp1257_lithuanian_ci("cp1257", "Cp1257", 29),
  latin5_turkish_ci("latin5", "ISO8859_9", 30),
  latin1_german2_ci("latin1", "Cp1252", 31),
  armscii8_general_ci("armscii8", "Cp1252", 32),
  utf8_general_ci("utf8", "UTF-8", 33),
  cp1250_czech_cs("cp1250", "Cp1250", 34),
  ucs2_general_ci("ucs2", "UnicodeBig", 35),
  cp866_general_ci("cp866", "Cp866", 36),
  keybcs2_general_ci("keybcs2", "Cp852", 37),
  macce_general_ci("macce", "MacCentralEurope", 38),
  macroman_general_ci("macroman", "MacRoman", 39),
  cp852_general_ci("cp852", "Cp852", 40),
  latin7_general_ci("latin7", "ISO-8859-13", 41),
  latin7_general_cs("latin7", "ISO-8859-13", 42),
  macce_bin("macce", "MacCentralEurope", 43),
  cp1250_croatian_ci("cp1250", "Cp1250", 44),
  utf8mb4_general_ci("utf8mb4", "UTF-8", 45),
  utf8mb4_bin("utf8mb4", "UTF-8", 46),
  latin1_bin("latin1", "Cp1252", 47),
  latin1_general_ci("latin1", "Cp1252", 48),
  latin1_general_cs("latin1", "Cp1252", 49),
  cp1251_bin("cp1251", "Cp1251", 50),
  cp1251_general_ci("cp1251", "Cp1251", 51),
  cp1251_general_cs("cp1251", "Cp1251", 52),
  macroman_bin("macroman", "MacRoman", 53),
  utf16_general_ci("utf16", "UTF-16", 54),
  utf16_bin("utf16", "UTF-16", 55),
  utf16le_general_ci("utf16le", "UTF-16LE", 56),
  cp1256_general_ci("cp1256", "Cp1256", 57),
  cp1257_bin("cp1257", "Cp1257", 58),
  cp1257_general_ci("cp1257", "Cp1257", 59),
  utf32_general_ci("utf32", "UTF-32", 60),
  utf32_bin("utf32", "UTF-32", 61),
  utf16le_bin("utf16le", "UTF-16LE", 62),
  binary("binary", "ISO8859_1", 63),
  armscii8_bin("armscii8", "Cp1252", 64),
  ascii_bin("ascii", "US-ASCII", 65),
  cp1250_bin("cp1250", "Cp1250", 66),
  cp1256_bin("cp1256", "Cp1256", 67),
  cp866_bin("cp866", "Cp866", 68),
  dec8_bin("dec8", "Cp1252", 69),
  greek_bin("greek", "ISO8859_7", 70),
  hebrew_bin("hebrew", "ISO8859_8", 71),
  hp8_bin("hp8", "Cp1252", 72),
  keybcs2_bin("keybcs2", "Cp852", 73),
  koi8r_bin("koi8r", "KOI8_R", 74),
  koi8u_bin("koi8u", "KOI8_R", 75),
  latin2_bin("latin2", "ISO8859_2", 77),
  latin5_bin("latin5", "ISO8859_9", 78),
  latin7_bin("latin7", "ISO-8859-13", 79),
  cp850_bin("cp850", "Cp850", 80),
  cp852_bin("cp852", "Cp852", 81),
  swe7_bin("swe7", "Cp1252", 82),
  utf8_bin("utf8", "UTF-8", 83),
  big5_bin("big5", "Big5", 84),
  euckr_bin("euckr", "EUC_KR", 85),
  gb2312_bin("gb2312", "EUC_CN", 86),
  gbk_bin("gbk", "GBK", 87),
  sjis_bin("sjis", "SJIS", 88),
  tis620_bin("tis620", "TIS620", 89),
  ucs2_bin("ucs2", "UnicodeBig", 90),
  ujis_bin("ujis", "EUC_JP", 91),
  geostd8_general_ci("geostd8", "Cp1252", 92),
  geostd8_bin("geostd8", "Cp1252", 93),
  latin1_spanish_ci("latin1", "Cp1252", 94),
  cp932_japanese_ci("cp932", "Cp932", 95),
  cp932_bin("cp932", "Cp932", 96),
  eucjpms_japanese_ci("eucjpms", "EUC_JP_Solaris", 97),
  eucjpms_bin("eucjpms", "EUC_JP_Solaris", 98),
  cp1250_polish_ci("cp1250", "Cp1250", 99),
  utf16_unicode_ci("utf16", "UTF-16", 101),
  utf16_icelandic_ci("utf16", "UTF-16", 102),
  utf16_latvian_ci("utf16", "UTF-16", 103),
  utf16_romanian_ci("utf16", "UTF-16", 104),
  utf16_slovenian_ci("utf16", "UTF-16", 105),
  utf16_polish_ci("utf16", "UTF-16", 106),
  utf16_estonian_ci("utf16", "UTF-16", 107),
  utf16_spanish_ci("utf16", "UTF-16", 108),
  utf16_swedish_ci("utf16", "UTF-16", 109),
  utf16_turkish_ci("utf16", "UTF-16", 110),
  utf16_czech_ci("utf16", "UTF-16", 111),
  utf16_danish_ci("utf16", "UTF-16", 112),
  utf16_lithuanian_ci("utf16", "UTF-16", 113),
  utf16_slovak_ci("utf16", "UTF-16", 114),
  utf16_spanish2_ci("utf16", "UTF-16", 115),
  utf16_roman_ci("utf16", "UTF-16", 116),
  utf16_persian_ci("utf16", "UTF-16", 117),
  utf16_esperanto_ci("utf16", "UTF-16", 118),
  utf16_hungarian_ci("utf16", "UTF-16", 119),
  utf16_sinhala_ci("utf16", "UTF-16", 120),
  utf16_german2_ci("utf16", "UTF-16", 121),
  utf16_croatian_ci("utf16", "UTF-16", 122),
  utf16_unicode_520_ci("utf16", "UTF-16", 123),
  utf16_vietnamese_ci("utf16", "UTF-16", 124),
  ucs2_unicode_ci("ucs2", "UnicodeBig", 128),
  ucs2_icelandic_ci("ucs2", "UnicodeBig", 129),
  ucs2_latvian_ci("ucs2", "UnicodeBig", 130),
  ucs2_romanian_ci("ucs2", "UnicodeBig", 131),
  ucs2_slovenian_ci("ucs2", "UnicodeBig", 132),
  ucs2_polish_ci("ucs2", "UnicodeBig", 133),
  ucs2_estonian_ci("ucs2", "UnicodeBig", 134),
  ucs2_spanish_ci("ucs2", "UnicodeBig", 135),
  ucs2_swedish_ci("ucs2", "UnicodeBig", 136),
  ucs2_turkish_ci("ucs2", "UnicodeBig", 137),
  ucs2_czech_ci("ucs2", "UnicodeBig", 138),
  ucs2_danish_ci("ucs2", "UnicodeBig", 139),
  ucs2_lithuanian_ci("ucs2", "UnicodeBig", 140),
  ucs2_slovak_ci("ucs2", "UnicodeBig", 141),
  ucs2_spanish2_ci("ucs2", "UnicodeBig", 142),
  ucs2_roman_ci("ucs2", "UnicodeBig", 143),
  ucs2_persian_ci("ucs2", "UnicodeBig", 144),
  ucs2_esperanto_ci("ucs2", "UnicodeBig", 145),
  ucs2_hungarian_ci("ucs2", "UnicodeBig", 146),
  ucs2_sinhala_ci("ucs2", "UnicodeBig", 147),
  ucs2_german2_ci("ucs2", "UnicodeBig", 148),
  ucs2_croatian_ci("ucs2", "UnicodeBig", 149),
  ucs2_unicode_520_ci("ucs2", "UnicodeBig", 150),
  ucs2_vietnamese_ci("ucs2", "UnicodeBig", 151),
  ucs2_general_mysql500_ci("ucs2", "UnicodeBig", 159),
  utf32_unicode_ci("utf32", "UTF-32", 160),
  utf32_icelandic_ci("utf32", "UTF-32", 161),
  utf32_latvian_ci("utf32", "UTF-32", 162),
  utf32_romanian_ci("utf32", "UTF-32", 163),
  utf32_slovenian_ci("utf32", "UTF-32", 164),
  utf32_polish_ci("utf32", "UTF-32", 165),
  utf32_estonian_ci("utf32", "UTF-32", 166),
  utf32_spanish_ci("utf32", "UTF-32", 167),
  utf32_swedish_ci("utf32", "UTF-32", 168),
  utf32_turkish_ci("utf32", "UTF-32", 169),
  utf32_czech_ci("utf32", "UTF-32", 170),
  utf32_danish_ci("utf32", "UTF-32", 171),
  utf32_lithuanian_ci("utf32", "UTF-32", 172),
  utf32_slovak_ci("utf32", "UTF-32", 173),
  utf32_spanish2_ci("utf32", "UTF-32", 174),
  utf32_roman_ci("utf32", "UTF-32", 175),
  utf32_persian_ci("utf32", "UTF-32", 176),
  utf32_esperanto_ci("utf32", "UTF-32", 177),
  utf32_hungarian_ci("utf32", "UTF-32", 178),
  utf32_sinhala_ci("utf32", "UTF-32", 179),
  utf32_german2_ci("utf32", "UTF-32", 180),
  utf32_croatian_ci("utf32", "UTF-32", 181),
  utf32_unicode_520_ci("utf32", "UTF-32", 182),
  utf32_vietnamese_ci("utf32", "UTF-32", 183),
  utf8_unicode_ci("utf8", "UTF-8", 192),
  utf8_icelandic_ci("utf8", "UTF-8", 193),
  utf8_latvian_ci("utf8", "UTF-8", 194),
  utf8_romanian_ci("utf8", "UTF-8", 195),
  utf8_slovenian_ci("utf8", "UTF-8", 196),
  utf8_polish_ci("utf8", "UTF-8", 197),
  utf8_estonian_ci("utf8", "UTF-8", 198),
  utf8_spanish_ci("utf8", "UTF-8", 199),
  utf8_swedish_ci("utf8", "UTF-8", 200),
  utf8_turkish_ci("utf8", "UTF-8", 201),
  utf8_czech_ci("utf8", "UTF-8", 202),
  utf8_danish_ci("utf8", "UTF-8", 203),
  utf8_lithuanian_ci("utf8", "UTF-8", 204),
  utf8_slovak_ci("utf8", "UTF-8", 205),
  utf8_spanish2_ci("utf8", "UTF-8", 206),
  utf8_roman_ci("utf8", "UTF-8", 207),
  utf8_persian_ci("utf8", "UTF-8", 208),
  utf8_esperanto_ci("utf8", "UTF-8", 209),
  utf8_hungarian_ci("utf8", "UTF-8", 210),
  utf8_sinhala_ci("utf8", "UTF-8", 211),
  utf8_german2_ci("utf8", "UTF-8", 212),
  utf8_croatian_ci("utf8", "UTF-8", 213),
  utf8_unicode_520_ci("utf8", "UTF-8", 214),
  utf8_vietnamese_ci("utf8", "UTF-8", 215),
  utf8_general_mysql500_ci("utf8", "UTF-8", 223),
  utf8mb4_unicode_ci("utf8mb4", "UTF-8", 224),
  utf8mb4_icelandic_ci("utf8mb4", "UTF-8", 225),
  utf8mb4_latvian_ci("utf8mb4", "UTF-8", 226),
  utf8mb4_romanian_ci("utf8mb4", "UTF-8", 227),
  utf8mb4_slovenian_ci("utf8mb4", "UTF-8", 228),
  utf8mb4_polish_ci("utf8mb4", "UTF-8", 229),
  utf8mb4_estonian_ci("utf8mb4", "UTF-8", 230),
  utf8mb4_spanish_ci("utf8mb4", "UTF-8", 231),
  utf8mb4_swedish_ci("utf8mb4", "UTF-8", 232),
  utf8mb4_turkish_ci("utf8mb4", "UTF-8", 233),
  utf8mb4_czech_ci("utf8mb4", "UTF-8", 234),
  utf8mb4_danish_ci("utf8mb4", "UTF-8", 235),
  utf8mb4_lithuanian_ci("utf8mb4", "UTF-8", 236),
  utf8mb4_slovak_ci("utf8mb4", "UTF-8", 237),
  utf8mb4_spanish2_ci("utf8mb4", "UTF-8", 238),
  utf8mb4_roman_ci("utf8mb4", "UTF-8", 239),
  utf8mb4_persian_ci("utf8mb4", "UTF-8", 240),
  utf8mb4_esperanto_ci("utf8mb4", "UTF-8", 241),
  utf8mb4_hungarian_ci("utf8mb4", "UTF-8", 242),
  utf8mb4_sinhala_ci("utf8mb4", "UTF-8", 243),
  utf8mb4_german2_ci("utf8mb4", "UTF-8", 244),
  utf8mb4_croatian_ci("utf8mb4", "UTF-8", 245),
  utf8mb4_unicode_520_ci("utf8mb4", "UTF-8", 246),
  utf8mb4_vietnamese_ci("utf8mb4", "UTF-8", 247),
  gb18030_chinese_ci("gb18030", "GB18030", 248),
  gb18030_bin("gb18030", "GB18030", 249),
  gb18030_unicode_520_ci("gb18030", "GB18030", 250),
  utf8mb4_0900_ai_ci("utf8mb4", "UTF-8", 255);

  private static final Logger LOGGER = LoggerFactory.getLogger(MySQLCollation.class);

  public static final List<String> SUPPORTED_COLLATION_NAMES = Arrays.stream(values()).map(Enum::name).collect(Collectors.toList());
  public static final List<String> SUPPORTED_CHARSET_NAMES = Arrays.stream(values()).map(MySQLCollation::mysqlCharsetName).distinct().collect(Collectors.toList());

  private static final Map<String, String> charsetToDefaultCollationMapping = new HashMap<>();
  private static final IntObjectMap<Charset> idToJavaCharsetMapping = new IntObjectHashMap<>();

  static {
    charsetToDefaultCollationMapping.put("big5", "big5_chinese_ci");
    charsetToDefaultCollationMapping.put("dec8", "dec8_swedish_ci");
    charsetToDefaultCollationMapping.put("cp850", "cp850_general_ci");
    charsetToDefaultCollationMapping.put("hp8", "hp8_english_ci");
    charsetToDefaultCollationMapping.put("koi8r", "koi8r_general_ci");
    charsetToDefaultCollationMapping.put("latin1", "latin1_swedish_ci");
    charsetToDefaultCollationMapping.put("latin2", "latin2_general_ci");
    charsetToDefaultCollationMapping.put("swe7", "swe7_swedish_ci");
    charsetToDefaultCollationMapping.put("ascii", "ascii_general_ci");
    charsetToDefaultCollationMapping.put("ujis", "ujis_japanese_ci");
    charsetToDefaultCollationMapping.put("sjis", "sjis_japanese_ci");
    charsetToDefaultCollationMapping.put("hebrew", "hebrew_general_ci");
    charsetToDefaultCollationMapping.put("tis620", "tis620_thai_ci");
    charsetToDefaultCollationMapping.put("euckr", "euckr_korean_ci");
    charsetToDefaultCollationMapping.put("koi8u", "koi8u_general_ci");
    charsetToDefaultCollationMapping.put("gb2312", "gb2312_chinese_ci");
    charsetToDefaultCollationMapping.put("greek", "greek_general_ci");
    charsetToDefaultCollationMapping.put("cp1250", "cp1250_general_ci");
    charsetToDefaultCollationMapping.put("gbk", "gbk_chinese_ci");
    charsetToDefaultCollationMapping.put("latin5", "latin5_turkish_ci");
    charsetToDefaultCollationMapping.put("armscii8", "armscii8_general_ci");
    charsetToDefaultCollationMapping.put("utf8", "utf8_general_ci");
    charsetToDefaultCollationMapping.put("ucs2", "ucs2_general_ci");
    charsetToDefaultCollationMapping.put("cp866", "cp866_general_ci");
    charsetToDefaultCollationMapping.put("keybcs2", "keybcs2_general_ci");
    charsetToDefaultCollationMapping.put("macce", "macce_general_ci");
    charsetToDefaultCollationMapping.put("macroman", "macroman_general_ci");
    charsetToDefaultCollationMapping.put("cp852", "cp852_general_ci");
    charsetToDefaultCollationMapping.put("latin7", "latin7_general_ci");
    charsetToDefaultCollationMapping.put("utf8mb4", "utf8mb4_general_ci");
    charsetToDefaultCollationMapping.put("cp1251", "cp1251_general_ci");
    charsetToDefaultCollationMapping.put("utf16", "utf16_general_ci");
    charsetToDefaultCollationMapping.put("utf16le", "utf16le_general_ci");
    charsetToDefaultCollationMapping.put("cp1256", "cp1256_general_ci");
    charsetToDefaultCollationMapping.put("cp1257", "cp1257_general_ci");
    charsetToDefaultCollationMapping.put("utf32", "utf32_general_ci");
    charsetToDefaultCollationMapping.put("binary", "binary");
    charsetToDefaultCollationMapping.put("geostd8", "geostd8_general_ci");
    charsetToDefaultCollationMapping.put("cp932", "cp932_japanese_ci");
    charsetToDefaultCollationMapping.put("eucjpms", "eucjpms_japanese_ci");
    charsetToDefaultCollationMapping.put("gb18030", "gb18030_chinese_ci");

    for (MySQLCollation collation : MySQLCollation.values()) {
      try {
        Charset charset = Charset.forName(collation.mappedJavaCharsetName);
        idToJavaCharsetMapping.put(collation.collationId, charset);
      } catch (Exception e) {
        LOGGER.warn("Java charset: [" + collation.mysqlCharsetName + "] is not supported by this platform.");
      }
    }
  }

  private final String mysqlCharsetName;
  private final String mappedJavaCharsetName;
  private final int collationId;

  MySQLCollation(String mysqlCharsetName, String mappedJavaCharsetName, int collationId) {
    this.mysqlCharsetName = mysqlCharsetName;
    this.mappedJavaCharsetName = mappedJavaCharsetName;
    this.collationId = collationId;
  }

  public static MySQLCollation valueOfName(String collationName) throws IllegalArgumentException {
    try {
      return MySQLCollation.valueOf(collationName);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unknown MySQL collation: [" + collationName + "]");
    }
  }

  /**
   * Get the Java charset with a correlative collation id.
   *
   * @param collationId id of the collation
   * @return the charset
   */
  public static Charset getJavaCharsetByCollationId(int collationId) {
    Charset charset = idToJavaCharsetMapping.get(collationId);
    if (charset == null) {
      throw new UnsupportedOperationException("Collation of Id [" + collationId + "] is unknown to this client");
    } else {
      return charset;
    }
  }

  public static String getDefaultCollationFromCharsetName(String charset) {
    String defaultCollationName = charsetToDefaultCollationMapping.get(charset);
    if (defaultCollationName == null) {
      throw new IllegalArgumentException("Unknown charset name: [" + charset + "]");
    } else {
      return defaultCollationName;
    }
  }

  /**
   * Get the binding MySQL charset name for this collation.
   *
   * @return the binding MySQL charset name
   */
  public String mysqlCharsetName() {
    return mysqlCharsetName;
  }

  /**
   * Get the mapped Java charset name which is mapped from the collation.
   *
   * @return the mapped Java charset name
   */
  public String mappedJavaCharsetName() {
    return mappedJavaCharsetName;
  }

  /**
   * Get the collation Id of this collation
   *
   * @return the collation Id
   */
  public int collationId() {
    return collationId;
  }
}
