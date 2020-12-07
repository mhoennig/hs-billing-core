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

/**
    Verification of some architecture rules, e.g. package depenencies.

    <p>There is a special git branch 'archunit-rule-validation' which deliberately breaks each rule.
    To verify changed arch rules, you can rebase that branch to the master branch
    and check if the rules are correct by running `./gradlew test`.</p>

    <p>See also 'resources/archunit.properties' for configuration
    and 'app/archunit_store' for the rules+results snapshots,
    both are only available in git branch 'archunit-rule-validation'.</p>

    <p>Please also add such "rule-tests" to that branch when you change these rules.</p>
 */
@AnalyzeClasses(packages = ["${ROOT_PACKAGE}.."])
internal class ArchitectureTest {
    @ArchTest
    val `will not have cycles between packages`: ArchRule = verify(
        slices().matching("${ROOT_PACKAGE}.(*)..")
            .should().beFreeOfCycles()
    )

    @ArchTest
    val `package 'lib' will not use any other package`: ArchRule = verify(
        noClasses()
            .that().resideInAPackage("..lib..")
            .should().dependOnClassesThat().resideInAnyPackage(
                *exceptPackageEndsWithAnyOf(".lib")
            )
    )

    @ArchTest
    val `package 'domain' will not use any other package except 'lib'`: ArchRule = verify(
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                *exceptPackageEndsWithAnyOf(".domain", ".lib")
            )
    )

    private fun exceptPackageEndsWithAnyOf(vararg packageEndings: String): Array<String> =
        ALL_PACKAGES.filter{
            !packageEndings.any{ ending -> it.endsWith(ending)}
        }.toTypedArray()
}

// in the git branch 'archunit-rule-validation', this function freezes the results
fun verify(archRule: ArchRule): ArchRule = archRule
