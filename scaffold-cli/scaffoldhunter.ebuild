# Copyright 1999-2013 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

EAPI=4

if use doc; then 
  EANT_BUILD_TARGET="dist-fat-jar build-doc"
else
  EANT_BUILD_TARGET="dist-fat-jar"
fi

inherit eutils java-pkg-2 java-ant-2

DESCRIPTION="A tool for the analysis of structure-related biochemical data"
HOMEPAGE="http://scaffoldhunter.sourceforge.net/"
SRC_URI="mirror://sourceforge/scaffoldhunter/scaffold-hunter-src-${PV}.tar.gz"

LICENSE="GPL-3"
SLOT="${PV%.*}"
KEYWORDS="~x86 ~sparc ~ppc ~amd64 ppc64"
IUSE="doc"

RDEPEND=">=virtual/jre-1.6"
DEPEND=">=virtual/jdk-1.6
    doc? ( app-text/texlive[extra,png] ) "

S=${WORKDIR}/release-${PV}


src_install() {
  java-pkg_newjar dist/scaffold-hunter-fat-${PV}.jar ${PN}.jar || die "packaging newjar failed"
  java-pkg_dolauncher ${PN} --java_args -Xmx2048m --jar || die "creation of launcher failed"
  doicon "resources/edu/udo/scaffoldhunter/resources/images/scaffoldhunter-icon.svg"
  make_desktop_entry scaffoldhunter "Scaffold Hunter" "scaffoldhunter-icon" "Science" "" || die "creation of desktop file failed"
  if use doc; then 
    dodoc build/doc/manual.pdf || die "installing manual failed"
  fi
}