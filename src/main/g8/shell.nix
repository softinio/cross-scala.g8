with (import <nixpkgs> {});
mkShell {
  buildInputs = [
    coursier
    bloop
    metals
    sbt
  ];
}
