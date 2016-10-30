import Player from "../models/player";
import ShotEvent from "../models/shotEvent";

export default Ember.Controller.extend({

  init: function () {
    var config = {
      apiKey: 'AIzaSyA6JlIw9KuU9XWXHb-qLsDwZH-dzjyl42U',
      authDomain: 'bstf-d63cc.firebaseapp.com',
      databaseURL: 'https://bstf-d63cc.firebaseio.com',
      storageBucket: 'bstf-d63cc.appspot.com',
    };

    let self = this;

    let database = firebase.initializeApp(config).database();
    self.set('database', database);
    this.set('gameSessionId', 1);
  },

  showGameSession: function (id) {
    let self = this;
    let databaseReference = this.get('database').ref('game_session_' + id);
    this.set('databaseReference', databaseReference);

    databaseReference.on('value', function (snapshot) {
      let snapshotValue = snapshot.val();
      console.log(snapshotValue);

      let shotsFired = Ember.A();

      if (!snapshotValue.shotsFired) {
        snapshotValue.shotsFired = [];
      }

      for (let i = 0; i < snapshotValue.shotsFired.length; i++) {
        if (!snapshotValue.shotsFired[i]) {
          continue;
        }

        shotsFired.pushObject(ShotEvent.create(snapshotValue.shotsFired[i]));
      }

      let players = Ember.A();

      if (!snapshotValue.players) {
        snapshotValue.players = [];
      }

      for (let i = 0; i < snapshotValue.players.length; i++) {
        if (!snapshotValue.players[i]) {
          continue;
        }

        let player = Player.create(snapshotValue.players[i]);
        player.set('shotsFired', shotsFired);
        players.pushObject(player);
      }

      shotsFired = self.sortAndFilterEvents(shotsFired);

      self.set('model', Ember.Object.create(snapshotValue));
      self.set('model.players', self.sortPlayers(players));
      self.set('model.shotsFired', shotsFired);
    });
  },

  sortAndFilterEvents: function (shotsFired) {
    if (shotsFired.length ==0) return shotsFired;
    return Ember.A(shotsFired.toArray().reverse());
  },

  sortPlayers: function (players) {
    if (players.length === 0) return players;

    let sortedByScore = players.sortBy('score');
    let result = Ember.A();

    for (let index = sortedByScore.length - 1; index != -1; index--) {
      result.pushObject(sortedByScore[index]);
    }

    return result;
  },

  actions: {
    openSession: function () {
      this.showGameSession(this.get('gameSessionId'));
    },

    stopSession: function () {
      let gameSession = {
        id: this.get('model.id'),
        players: [],
        started: false
      };

      this.get('databaseReference').set(gameSession);
    }
  },

  anyPlayers: Ember.computed('model.players', function () {
    let players = this.get('model.players');
    return players && players.length > 0;
  })
})
