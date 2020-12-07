package org.hostsharing.hsadmin.billing.core

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

internal const val ROOT_PACKAGE = "org.hostsharing.hsadmin.billing.core"
internal val ALL_PACKAGES = arrayOf(
    ROOT_PACKAGE,
    "${ROOT_PACKAGE}.domain",
    "${ROOT_PACKAGE}.reader",
    "${ROOT_PACKAGE}.writer",
    "${ROOT_PACKAGE}.lib")

@AnalyzeClasses(packages = ["${ROOT_PACKAGE}.."])
internal class ArchitectureTest {
    @ArchTest
    val `will not have cycles between packages`: ArchRule =
        slices().matching("${ROOT_PACKAGE}.(*)..")
            .should().beFreeOfCycles()

    @ArchTest
    val `package 'lib' will not use any other package`: ArchRule =
        noClasses()
            .that().resideInAPackage("..lib..")
            .should().dependOnClassesThat().resideInAnyPackage(
                *exceptPackageEndsWithAnyOf(".lib")
            )

    @ArchTest
    val `package 'domain' will not use any other package except 'lib'`: ArchRule =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                *exceptPackageEndsWithAnyOf(".domain", ".lib")
            )

    private fun exceptPackageEndsWithAnyOf(vararg packageEndings: String): Array<String> =
        ALL_PACKAGES.filter{
            !packageEndings.any{ ending -> it.endsWith(ending)}
        }.toTypedArray()

}
