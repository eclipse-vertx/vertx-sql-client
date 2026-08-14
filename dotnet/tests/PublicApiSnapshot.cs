/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Reflection;
using System.Text;

namespace Apex.Tests.Shared;

internal static class PublicApiSnapshot
{
  public static void Verify(Assembly assembly, string fileName)
  {
    string actual = Generate(assembly);
    string? updateDirectory = Environment.GetEnvironmentVariable("APEX_UPDATE_PUBLIC_API");
    if (!string.IsNullOrEmpty(updateDirectory))
    {
      Directory.CreateDirectory(updateDirectory);
      File.WriteAllText(Path.Combine(updateDirectory, fileName), actual);
      return;
    }

    string path = Path.Combine(AppContext.BaseDirectory, "PublicApi", fileName);
    string expected = File.ReadAllText(path);
    Assert.AreEqual(expected, actual, $"Public API changed. Review it and update {fileName} intentionally.");
  }

  private static string Generate(Assembly assembly)
  {
    StringBuilder output = new();
    foreach (Type type in assembly.GetExportedTypes().OrderBy(static type => type.FullName, StringComparer.Ordinal))
    {
      output.AppendLine(TypeDeclaration(type));
      IEnumerable<MemberInfo> members = type.GetMembers(
          BindingFlags.Public |
          BindingFlags.Instance |
          BindingFlags.Static |
          BindingFlags.DeclaredOnly)
        .Where(static member => member.MemberType != MemberTypes.NestedType)
        .OrderBy(static member => member.MemberType)
        .ThenBy(static member => member.ToString(), StringComparer.Ordinal);
      foreach (MemberInfo member in members)
      {
        output.Append("  ").Append(member.MemberType).Append(": ").AppendLine(member.ToString());
      }
    }

    return output.ToString();
  }

  private static string TypeDeclaration(Type type)
  {
    string kind = type.IsInterface
      ? "interface"
      : type.IsEnum
        ? "enum"
        : type.IsValueType
          ? "struct"
          : "class";
    string inheritance = type.BaseType is null || type.BaseType == typeof(object) ||
                         type.BaseType == typeof(ValueType) || type.BaseType == typeof(Enum)
      ? string.Empty
      : $" : {type.BaseType}";
    string interfaces = string.Join(
      ", ",
      type.GetInterfaces()
        .Select(static implemented => implemented.ToString())
        .Order(StringComparer.Ordinal));
    if (interfaces.Length > 0)
    {
      inheritance += inheritance.Length == 0 ? $" : {interfaces}" : $", {interfaces}";
    }

    return $"{kind} {type.FullName}{inheritance}";
  }
}
