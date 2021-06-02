package io.vertx.pgclient.data;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CharacterTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testName(TestContext ctx) {
    testDecodeGeneric(ctx, "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X", "NAME", "Name", Tuple::getString, Row::getString,
      "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ");
  }

  @Test
  public void testBlankPaddedChar(TestContext ctx) {
    testDecodeGeneric(ctx, "pgClient", "CHAR(15)", "Char", Tuple::getString, Row::getString, "pgClient       ");
  }

  @Test
  public void testSingleBlankPaddedChar(TestContext ctx) {
    testDecodeGeneric(ctx, "V", "CHAR", "Char", Tuple::getString, Row::getString, "V");
  }

  @Test
  public void testSingleChar(TestContext ctx) {
    testDecodeGeneric(ctx, "X", "CHAR", "Character", Tuple::getString, Row::getString, "X");
  }

  @Test
  public void testVarChar(TestContext ctx) {
    testDecodeGeneric(ctx, "pgClient", "VARCHAR(15)", "Driver", Tuple::getString, Row::getString, "pgClient");
  }

  @Test
  public void testText(TestContext ctx) {
    testDecodeGeneric(ctx, "Vert.x PostgreSQL Client", "TEXT", "Text", Tuple::getString, Row::getString, "Vert.x PostgreSQL Client");
  }

  @Test
  public void testDecodeCHARArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY['01234567' :: CHAR(8)]", "CharArray", Tuple::getArrayOfStrings, Row::getArrayOfStrings, "01234567");
  }

  @Test
  public void testDecodeTEXTArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['Knock, knock.Who’s there?very long pause….Java.' :: TEXT]", "TextArray", Tuple::getArrayOfStrings, Row::getArrayOfStrings, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeTEXTArray2(TestContext ctx) {
    // \ is escaped when reading the text array and we get {foo,"bar\\"}
    // as it is the last char of the second element we need to ensure
    // that the self escape (\\) does not escape the ending "
    testDecodeGenericArray(ctx, "ARRAY ['foo' :: TEXT, 'bar\\' :: TEXT]", "TextArray", Tuple::getArrayOfStrings, Row::getArrayOfStrings, "foo", "bar\\");
  }

  @Test
  public void testDecodeVARCHARArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['Knock, knock.Who’s there?very long pause….Java.' :: VARCHAR]", "VarcharArray", Tuple::getArrayOfStrings, Row::getArrayOfStrings, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeNAMEArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['Knock, knock.Who’s there?very long pause….Java.' :: NAME]", "NameArray", Tuple::getArrayOfStrings, Row::getArrayOfStrings, "Knock, knock.Who’s there?very long pause….Java.");
  }
}
