buildInfoKeys := Seq[BuildInfoKey](
  startYear,
  organization,
  organizationName,
  organizationHomepage,
  scalacOptions,
  javacOptions,
  version,
  scalaVersion,
  sbtVersion,
  sbtBinaryVersion,
  git.gitCurrentTags,
  git.gitDescribedVersion,
  git.gitCurrentBranch,
  git.gitHeadCommit,
  git.gitHeadCommitDate)

import Dependencies._

buildInfoKeys ++= Seq(
  "versionScalaXml" -> versionScalaXml,
  "versionScalaCollectionCompat" -> versionScalaCollectionCompat,
  "versionJava8Compat" -> versionJava8Compat,
  "versionScalameta" -> versionScalameta,
  "versionScalatest" -> versionScalatest,
  "versionAkka" -> versionAkka,
  "versionAkkaManagement" -> versionAkkaManagement,
  "versionAkkaHttp" -> versionAkkaHttp,
  "versionAkkaHttpCors" -> versionAkkaHttpCors,
  "versionAlpakka" -> versionAlpakka,
  "versionAlpakkaKafka" -> versionAlpakkaKafka,

  "versionScalapbJson4s" -> versionScalapbJson4s
)
