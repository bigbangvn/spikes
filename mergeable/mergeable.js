const GitHub = require('github-api');
const Slack = require('@slack/client').WebClient;
const secrets = require('./secrets');

const OPEN_PRS = { state: 'open' }

function Mergeable(secrets) {
  this.gitHubSecrets = secrets.gitHub;
  this.gitHub = new GitHub(secrets.gitHub.credentials);
  this.slack = new Slack(secrets.slack.token);
}

Mergeable.prototype.checkMergeability = function() {
  const repo = this.gitHub.getRepo(
    this.gitHubSecrets.repoOwner,
    this.gitHubSecrets.repoName
  );
  return repo.listPullRequests(OPEN_PRS)
    .then(getIndividualPullRequests(repo))
    .then(findUnmergeablePrs)
    .then(notifySlack);
}

function getIndividualPullRequests(repo) {
  return function(listOfPrs) {
    const allPrs = listOfPrs.data.map(each => {
      return repo.getPullRequest(each.number)
        .then(toData);
    });
    return Promise.all(allPrs);
  }
}

function toData(pullRequest) {
  return Promise.resolve(pullRequest.data);
}

function findUnmergeablePrs(pullRequests) {
  const unmergablePrs = pullRequests.filter(filterUnmergeable)
  return Promise.resolve(unmergablePrs);
}

const filterUnmergeable = (pr) => !pr.mergeable;

function notifySlack(unmergeablePrs) {
  return Promise.all(unmergeablePrs.map(each => {
    return this.slack.chat.postMessage(secrets.slack.recipient, createSlackMessage(each), { as_user: true})
      .then(Promise.resolve(unmergeablePrs));
  }));
}

function createSlackMessage(pr) {
  return `<${pr.html_url}|${pr.title}> has conflicts with master!`;
}

module.exports = Mergeable;