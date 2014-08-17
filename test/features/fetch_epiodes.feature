Feature: Episodes are fetched from Nitro
  As IBL
  I want to fetch episodes from Nitro into a Cache
  So that we can deliver fast services to our clients

  Scenario: Episode exists in Nitro
    Given an episode with pid "p00lfrb3" exists in Nitro
    When I request IBL episode "p00lfrb3"
    Then it returns 200 response
    And the episode has the expected attributes
#    And the episode has the following attributes:
#      | pid      | parent_id | release_date |
#      | p00lfrb3 | b00ffbjg  | 12 Nov 1972  |

  Scenario: Episode does not exist in Nitro
    Given an episode with pid "p00lfrb3" doesn't exists in Nitro
    When I request IBL episode "p00lfrb3"
    Then it returns 404 response