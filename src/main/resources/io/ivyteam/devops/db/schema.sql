CREATE TABLE user (
    name VARCHAR(200) NOT NULL,
    avatarUrl VARCHAR(200) NULL,
    PRIMARY KEY (name)
);

CREATE TABLE repository (
    name VARCHAR(200) PRIMARY KEY NOT NULL,
    archived INTEGER NOT NULL,
    private INTEGER NOT NULL,
    deleteBranchOnMerge INTEGER NOT NULL,
    projects INTEGER NOT NULL,
    issues INTEGER NOT NULL,
    wiki INTEGER NOT NULL,
    hooks INTEGER NOT NULL,
    fork INTEGER NOT NULL,
    isVulnAlertOn INTEGER NOT NULL,
    license TEXT NULL,
    securityMd TEXT NULL,
    codeOfConduct TEXT NULL
);

CREATE TABLE pull_request (
    repository VARCHAR(200) NOT NULL,
    id INTEGER NOT NULL,
    title VARCHAR(400) NOT NULL,
    user VARCHAR(200) NOT NULL,
    branchName VARCHAR(400) NOT NULL,
    PRIMARY KEY (repository, id),
    FOREIGN KEY (repository) REFERENCES repository (name) ON DELETE CASCADE,
    FOREIGN KEY (user) REFERENCES user (name) ON DELETE CASCADE
);

CREATE TABLE branch (
    repository VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    lastCommitAuthor VARCHAR(200) NOT NULL,
    protected INTEGER NOT NULL,
    authoredDate DATE NOT NULL,
    PRIMARY KEY (repository, name),
    FOREIGN KEY (repository) REFERENCES repository (name) ON DELETE CASCADE,
    FOREIGN KEY (lastCommitAuthor) REFERENCES user (name) ON DELETE CASCADE
);

CREATE TABLE dependabot (
    repository VARCHAR(200) NOT NULL,
    critical INTEGER NOT NULL,
    high INTEGER NOT NULL,
    medium INTEGER NOT NULL,
    low INTEGER NOT NULL,
    PRIMARY KEY (repository)
    FOREIGN KEY (repository) REFERENCES repository (name) ON DELETE CASCADE
);