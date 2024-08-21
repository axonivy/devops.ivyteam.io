CREATE TABLE repository (
    name VARCHAR(200) PRIMARY KEY NOT NULL,
    archived INTEGER NOT NULL,
    openPullRequests INTEGER NOT NULL,
    license INTEGER NOT NULL,
    settingsLog TEXT NULL
);

CREATE TABLE pull_request (
    repository VARCHAR(200) NOT NULL,
    id INTEGER NOT NULL,
    title VARCHAR(400) NOT NULL,
    user VARCHAR(200) NOT NULL,
    PRIMARY KEY (repository, id),
    FOREIGN KEY (repository) REFERENCES repository (name) ON DELETE CASCADE
);

CREATE TABLE branch (
    repository VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    lastCommitAuthor VARCHAR(200) NOT NULL,
    PRIMARY KEY (repository, name),
    FOREIGN KEY (repository) REFERENCES repository (name) ON DELETE CASCADE
);